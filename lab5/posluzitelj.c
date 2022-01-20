#include "queue.h"
#include "util.h"

#include <errno.h>
#include <fcntl.h>
#include <mqueue.h>
#include <pthread.h>
#include <signal.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <time.h>

#define ENV_VAR "SRSV_LAB5"
#define ITER_THRESHOLD 1E-3

#define BUF_SIZE MSG_MAXSIZE

static unsigned N;
static unsigned M;

void usage(void);
void determine_num_iter(void);
void posluzitelj(void);
void *radna(void *p);
void input(char *const *argv);
void create_threads(pthread_t *threads, char arr[N][10]);
void join_threads(const pthread_t *threads, const long *statuses);
void cleanup(void);
void sigmask_posluzitelj(void);
void enqueue_msg(const char *buf);
void reset_alarm(int sec);
void sigmask_radna(void);
void obrada_podataka(const void *p, const struct job_descriptor *job,
                     int *nums);
ssize_t my_mq_receive(mqd_t mqdes, char *buf);
void my_create_alrm();

void set_posluzitelj_sched();
static unsigned long long num_iter = 300000000ULL;
static volatile bool receive = true;
static volatile bool start_workers = false;

static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t cond = PTHREAD_COND_INITIALIZER;
static struct queue queue = {.cond = &cond,
                             .mutex = &mutex,
                             .N_sum = 0,
                             .M_sum = 0,
                             .head = NULL,
                             .tail = NULL};
static timer_t timer;

void signal_handler(int sig, siginfo_t *siginfo, void *v) {
  (void)sig;
  (void)siginfo;
  (void)v;

  debug_print("signal_handler %s", "\n");
  receive = false;
}

void alarm_handler(int sig, siginfo_t *siginfo, void *v) {
  (void)sig;
  (void)siginfo;
  (void)v;
  debug_print("alarm_handler %s", "\n");
  start_workers = true;
  pthread_cond_broadcast(&cond);
}

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
  debug_print("M: %u, N: %u\n", M, N);

  determine_num_iter();
  debug_print("num_iter: %llu\n", num_iter);

  set_posluzitelj_sched();

  pthread_t threads[N];
  long statuses[N];
  char arg[N][10];
  create_threads(threads, arg);

  posluzitelj();

  join_threads(threads, statuses);
  cleanup();
  return 0;
}

void set_posluzitelj_sched() {
  int policy = SCHED_RR;
  struct sched_param prio;

  /* set scheduling policy and priority for main thread */
  prio.sched_priority = 60;
  if (pthread_setschedparam(pthread_self(), policy, &prio)) {
    perror("Error: pthread_setschedparam (root permission?)");
    exit(1);
  }
}

void cleanup(void) {
  debug_print("cleanup: %s", "\n");
  pthread_mutex_destroy(&mutex);
  pthread_cond_destroy(&cond);
}

void join_threads(const pthread_t *threads, const long *statuses) {
  debug_print("join_threads: %s", "\n");
  for (unsigned i = 0; i < N; i++) {
    pthread_cond_broadcast(&cond);
    const long *ptr = statuses + i;
    pthread_join(threads[i], (void *)&ptr);
    debug_print("join_thread: %u\n", i);
  }
}

void create_threads(pthread_t *threads, char arr[N][10]) {
  int policy = SCHED_RR;
  struct sched_param prio = {.sched_priority = 40};
  pthread_attr_t attr;

  /* define scheduling properties for new threads */
  pthread_attr_init(&attr);
  pthread_attr_setinheritsched(&attr, PTHREAD_EXPLICIT_SCHED);
  pthread_attr_setschedpolicy(&attr, policy);
  pthread_attr_setschedparam(&attr, &prio);

  for (unsigned i = 0; i < N; i++) {
    memset(arr[i], '\0', 10);
    sprintf(arr[i], "R%d", i);
    CALL(ACT_STOP, pthread_create, threads + i, &attr, radna, arr[i]);
    debug_print("created_thread: %u\n", i);
  }
}

void input(char *const *argv) {
  N = (unsigned)strtoul(argv[1], NULL, 10);
  M = (unsigned)strtoul(argv[2], NULL, 10);
}

void posluzitelj() {
  debug_print("posluzitelj: %s", "\n");
  print_thread_scheduling_parameters("posluzitelj");
  mqd_t mqdes;
  char name[BUF_SIZE];
  memset(name, '\0', BUF_SIZE);
  name[0] = '/';
  strcat(name, getenv(ENV_VAR));

  debug_print("SRS_LAB5: %s\n", name);

  sigmask_posluzitelj();
  my_mqopen(name, &mqdes, O_RDONLY | O_CREAT);

  char buf[BUF_SIZE];
  buf[BUF_SIZE - 1] = '\0';

  my_create_alrm();

  while (receive) {
    ssize_t msglen = my_mq_receive(mqdes, buf);
    buf[msglen] = '\0';

    if (msglen >= 0) {
      printf("P: zaprimio %s\n", buf);
      enqueue_msg(buf);
      reset_alarm(30);
    }
  }

  debug_print("posluzitelj done: %s", "\n");
  mq_unlink(name);
}

void my_create_alrm() {
  struct sigevent event;
  event.sigev_notify = SIGEV_SIGNAL;
  event.sigev_signo = SIGALRM;

  CALL(ACT_STOP, timer_create, CLOCK_MONOTONIC, &event, &timer);
}

ssize_t my_mq_receive(mqd_t mqdes, char *buf) {
  while (true) {
    ssize_t msglen = mq_receive(mqdes, buf, BUF_SIZE, NULL);
    if (msglen < 0) {
      if (errno == EINTR && receive) {
        continue;
      } else if (receive) {
        perror("mq_receive");
        exit(EXIT_FAILURE);
      }
    }
    return msglen;
  }
}

void reset_alarm(int sec) {
  debug_print("reset_alarm %s", "\n");
  struct itimerspec period;

  period.it_value.tv_sec = sec;
  period.it_value.tv_nsec = period.it_interval.tv_nsec =
      period.it_interval.tv_sec = 0;
  CALL(ACT_STOP, timer_settime, timer, 0, &period, NULL);
}

void enqueue_msg(const char *buf) {
  struct job_descriptor *jd = malloc(sizeof(struct job_descriptor));
  sscanf(buf, "%u %u %s", &jd->id, &jd->t, jd->name);
  debug_print("enqueue_msg: id:%u t:%u name:%s\n", jd->id, jd->t, jd->name);
  enqueue(&queue, jd);
  if (queue.M_sum >= M && queue.N_sum >= N) {
    debug_print("cond_broadcast %s", "\n");
    start_workers = true;
    pthread_cond_broadcast(&cond);
  }
}

void sigmask_posluzitelj(void) {
  debug_print("sigmask_posluzitelj %s", "\n");
  struct sigaction act;
  act.sa_flags = 0;
  act.sa_sigaction = signal_handler;
  sigemptyset(&act.sa_mask);
  sigaddset(&act.sa_mask, SIGALRM);
  CALL(ACT_STOP, sigaction, SIGTERM, &act, NULL);

  act.sa_sigaction = alarm_handler;
  sigemptyset(&act.sa_mask);
  sigaddset(&act.sa_mask, SIGTERM);
  CALL(ACT_STOP, sigaction, SIGALRM, &act, NULL);
}

bool wait_cond(void) {
  debug_print("start_workers: %d, N_sum: %u, receive: %d\n", start_workers,
              queue.N_sum, receive);
  return !start_workers || (queue.N_sum == 0 && receive);
} // [dok workeri ne rade] ili [dok god se primaju poruke (posluzitelj radi), a
  // red poslova je prazan]

void *radna(void *p) {
  debug_print("radna %10s\n", (char *)p);
  print_thread_scheduling_parameters((char *)p);
  sigmask_radna();

  while (wait_cond() || queue.N_sum > 0) {
    debug_print("deque %s", "\n");

    struct job_descriptor *job = dequeue(&queue, true, wait_cond);
    if (job == NULL && !receive) // kraj
      break;
    else if (job == NULL) {
      fprintf(stderr, "dequeue: retval == NULL\n");
      exit(EXIT_FAILURE);
    }

    reset_alarm(0);

    debug_print("deque_job id: %u, t: %u, name: %s\n", job->id, job->t,
                job->name);
    int id = my_shm_open(job->name, O_RDWR, 00600);

    int *nums = memory_map(job->t * sizeof(int), job->name,
                           PROT_READ | PROT_WRITE, MAP_SHARED, id);

    obrada_podataka(p, job, nums);

    munmap(nums, job->t * sizeof(int));
    shm_unlink(job->name);
    free(job);
  }

  printf("thread: %s done\n", (char *)p);
  return 0;
}

void obrada_podataka(const void *p, const struct job_descriptor *job,
                     int *nums) {
  for (unsigned i = 1; i <= job->t; i++) {
    printf("%s: id:%u obrada podatka: %d (%u/%u)\n", (char *)p, job->id,
           nums[i - 1], i, job->t);
    for (unsigned long long j = 0; j < num_iter; j++) {
      asm volatile("" ::: "memory");
    }
  }
  printf("%s: id:%u obrada gotova\n", (char *)p, job->id);
}

void sigmask_radna(void) {
  sigset_t sigmask;
  sigemptyset(&sigmask);
  sigaddset(&sigmask, SIGTERM);
  sigaddset(&sigmask, SIGALRM);
  CALL(ACT_STOP, pthread_sigmask, SIG_BLOCK, &sigmask, NULL);
}

void determine_num_iter(void) {
  struct timespec tp;
  time_t sstart;
  long nsstart;
  double duration;

  while (true) {
    CALL(ACT_STOP, clock_gettime, CLOCK_MONOTONIC, &tp);
    sstart = tp.tv_sec;
    nsstart = tp.tv_nsec;

    for (unsigned long long i = 0; i < num_iter; i++) {
      asm volatile("" ::: "memory");
    }

    CALL(ACT_STOP, clock_gettime, CLOCK_MONOTONIC, &tp);

    duration = tp.tv_sec - sstart + (tp.tv_nsec - nsstart) * 1E-9;

    if (abs(duration - 1) < ITER_THRESHOLD)
      break;

    num_iter = num_iter / duration;
  }
}

void usage() {
  fprintf(stderr, "./posluzitelj N M\n");
  exit(EXIT_FAILURE);
}
