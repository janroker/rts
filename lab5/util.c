#include "util.h"

int random_int(int below) {
  double q = rand() / ((double)((long)RAND_MAX + 1)); // 0 - 0.99...
  return ((int)(q * below));
}

void my_mqopen(const char *var1, mqd_t *mqdes, int oflag) {
  struct mq_attr attr = {.mq_curmsgs = 0,
                         .mq_flags = 0,
                         .mq_maxmsg = MSG_MAXNUM,
                         .mq_msgsize = MSG_MAXSIZE};
  *mqdes = mq_open(var1, oflag, 00600, &attr);
  if (*mqdes == -1) {
    perror("mq_open");
    exit(EXIT_FAILURE);
  }

  if (mq_getattr(*mqdes, &attr) == -1) {
    perror("mq_open");
    exit(EXIT_FAILURE);
  }
  debug_print("mq_attr: mq_msgsize: %ld, mq_maxmsg: %ld, mq_flags: %ld, "
              "mq_curmsgs %ld\n",
              attr.mq_msgsize, attr.mq_maxmsg, attr.mq_flags, attr.mq_curmsgs);
}

void *memory_map(size_t len, char *name, int prot, int flags, int fd) {
  debug_print("memory_map %s\n", name);
  void *retval = mmap(NULL, len, prot, flags, fd, 0);
  if (retval == (void *)-1) {
    perror("mmap");
    shm_unlink(name);
    exit(EXIT_FAILURE);
  }
  close(fd);

  return retval;
}

int my_shm_open(const char *name, int oflag, int mode) {
  debug_print("my_shm_open %s\n", name);
  int ids_desc = shm_open(name, oflag, mode);
  if (ids_desc == -1) {
    perror("shm_open");
    exit(EXIT_FAILURE);
  }

  return ids_desc;
}
