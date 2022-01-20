#include "util.h"

#include <errno.h>
#include <pthread.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#define ENV_VAR "SRSV_LAB5"
#define BUF_SIZE MSG_MAXSIZE

static unsigned J;
static unsigned K;
static mqd_t mqdes;

struct binary_semaphore {
  pthread_mutex_t mutex;
  pthread_cond_t cvar;
  bool v;
};

static struct ids {
  unsigned id;
  unsigned ref_cnt;
  struct binary_semaphore sem;
} * ids;

void usage();
void generator(char *name);
void input(char *const *argv);
void mqOpen(char *name);
void create_id_shm(char *name);
unsigned int get_ids();
unsigned int get_job_duration();
size_t create_name_and_posao_str(const char *name, char *pbuf,
                                 unsigned int f_id, unsigned int t,
                                 struct job_descriptor *jd);
void generate_nums(unsigned int t, char *pbuf, size_t len, int nums[]);
void send_job(struct job_descriptor *jd);
void mysem_post(struct binary_semaphore *p);
void mysem_wait(struct binary_semaphore *p);
void mysem_destroy(struct binary_semaphore *pSemaphore);
void set_sched();

static void print_thread_scheduling_parameters(char *th) {
  int policy;
  struct sched_param prio;

  pthread_getschedparam(pthread_self(), &policy, &prio);
  debug_print("Thread %s: policy=%d, prio=%d\n", th, policy,
              prio.sched_priority);
}

int main(int argc, char *argv[]) {
  if (argc != 3) {
    usage();
  }
  input(argv);
  srand(time(NULL));

  char name[BUF_SIZE];
  memset(name, '\0', BUF_SIZE);
  name[0] = '/';
  strncat(name, getenv(ENV_VAR), BUF_SIZE);
  debug_print("name: %s\n", name);

  set_sched();

  mqOpen(name);
  create_id_shm(name);
  generator(name);

  mysem_wait(&ids->sem);
  ids->ref_cnt--;
  mysem_post(&ids->sem);
  //  if (ids->ref_cnt == 0) {
  //    debug_print("mysem_destroy: %s", "\n");
  //    mysem_destroy(&ids->sem);
  //    munmap(ids, sizeof(struct ids));
  //    shm_unlink(name);
  //  }
  debug_print("generator exit 0 %s", "\n");
  return 0;
}

void set_sched() {
  int policy = SCHED_RR;
  struct sched_param prio;

  /* set scheduling policy and priority for main thread */
  prio.sched_priority = 50;
  if (pthread_setschedparam(pthread_self(), policy, &prio)) {
    perror("Error: pthread_setschedparam (root permission?)");
    exit(1);
  }
}

void mysem_destroy(struct binary_semaphore *pSemaphore) {
  debug_print("mysem_destroy: %s", "\n");
  pthread_mutex_destroy(&pSemaphore->mutex);
  pthread_cond_destroy(&pSemaphore->cvar);
}

void create_id_shm(char *name) {
  debug_print("create_id_shm: %s\n", name);

  bool created = false;
  int ids_desc = shm_open(name, O_EXCL | O_CREAT | O_RDWR, 00600);
  if (ids_desc == -1) {
    if (errno == EEXIST) {
      ids_desc = shm_open(name, O_RDWR, 00600);
    }

    if (ids_desc == -1) {
      perror("shm_open");
      exit(EXIT_FAILURE);
    }
  } else {
    created = true;
  }
  CALL(ACT_STOP, ftruncate, ids_desc, sizeof(struct ids));

  ids = memory_map(sizeof(struct ids), name, PROT_READ | PROT_WRITE, MAP_SHARED,
                   ids_desc);

  if (created) {
    debug_print("init_ids: %s", "\n");
    ids->id = 1;
    ids->ref_cnt = 0;
    pthread_mutex_init(&ids->sem.mutex, NULL);
    pthread_cond_init(&ids->sem.cvar, NULL);
    ids->sem.v = true;
    debug_print("init_ids_done: %s", "\n");
  }
  ids->ref_cnt++;
  debug_print("ids_ref_cnt: %u\n", ids->ref_cnt);
}

void mqOpen(char *name) { my_mqopen(name, &mqdes, O_WRONLY | O_CREAT); }

void input(char *const *argv) {
  J = (unsigned)strtoul(argv[1], NULL, 10);
  K = (unsigned)strtoul(argv[2], NULL, 10);
}

void generator(char *name) {
  debug_print("generator: %s", "\n");
  print_thread_scheduling_parameters("generator");
  unsigned int f_id = get_ids();

  for (unsigned i = 1; i <= J; i++) {
    sleep(1);
    unsigned int t = get_job_duration();

    struct job_descriptor jd = {.id = f_id, .t = t};

    char pbuf[BUF_SIZE];
    size_t len = create_name_and_posao_str(name, pbuf, f_id, t, &jd);

    int nums[t];
    generate_nums(t, pbuf, len, nums);
    pbuf[BUF_SIZE - 1] = '\0';
    printf("%s]\n", pbuf);

    int ids_desc = my_shm_open(jd.name, O_CREAT | O_RDWR, 00600);

    CALL(ACT_STOP, ftruncate, ids_desc, sizeof(nums));

    int *numsMapped = memory_map(sizeof(nums), jd.name, PROT_READ | PROT_WRITE,
                                 MAP_SHARED, ids_desc);

    memcpy(numsMapped, nums, sizeof(nums));
    munmap(numsMapped, sizeof(nums));

    send_job(&jd);

    f_id++;
  }
  debug_print("generator_done: %s", "\n");
}

void send_job(struct job_descriptor *jd) {
  debug_print("send_job %u, %u, %s\n", jd->id, jd->t, jd->name);
  char buf[BUF_SIZE];
  sprintf(buf, "%d %d %s", (*jd).id, (*jd).t, (*jd).name);
  if (mq_send(mqdes, buf, strnlen(buf, BUF_SIZE), 0)) {
    perror("mq_send");
    exit(EXIT_FAILURE);
  }
  debug_print("send_job_done %u\n", jd->id);
}

void generate_nums(unsigned int t, char *pbuf, size_t len, int nums[]) {
  debug_print("generate_nums: %s", "\n");
  for (unsigned j = 0; j < t; j++) {
    nums[j] = random_int(1000) + 1;
    sprintf(pbuf + len, "%d ", nums[j]);
    len = strnlen(pbuf, 512);
  }
}

size_t create_name_and_posao_str(const char *name, char *pbuf,
                                 unsigned int f_id, unsigned int t,
                                 struct job_descriptor *jd) {

  sprintf(jd->name, "%s-%u", name, f_id);
  sprintf(pbuf, "G: posao %u %u %s [ ", f_id, t, (*jd).name);
  size_t len = strnlen(pbuf, BUF_SIZE);
  return len;
}

unsigned int get_job_duration() {
  unsigned t = random_int((int)K);
  t++;
  return t;
}

unsigned int get_ids() {
  mysem_wait(&ids->sem);
  unsigned f_id = ids->id;
  ids->id = f_id + J;
  mysem_post(&ids->sem);
  debug_print("get_ids: %u\n", f_id);
  return f_id;
}

void usage() {
  fprintf(stderr, "./posluzitelj J K\n");
  exit(EXIT_FAILURE);
}

void mysem_post(struct binary_semaphore *p) {
  pthread_mutex_lock(&p->mutex);
  if (p->v)
    abort();
  p->v = true;
  pthread_cond_signal(&p->cvar);
  pthread_mutex_unlock(&p->mutex);
}

void mysem_wait(struct binary_semaphore *p) {
  pthread_mutex_lock(&p->mutex);
  while (!p->v)
    pthread_cond_wait(&p->cvar, &p->mutex);
  p->v = false;
  pthread_mutex_unlock(&p->mutex);
}
