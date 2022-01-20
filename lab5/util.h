#ifndef UTIL_H
#define UTIL_H

#include <fcntl.h>
#include <mqueue.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>

#ifdef DEBUG
#define DEBUG_TEST 1
#else
#define DEBUG_TEST 0
#endif

#define debug_print(fmt, ...)                                                  \
  do {                                                                         \
    if (DEBUG_TEST)                                                            \
      fprintf(stderr, "%s:%d:%s(): " fmt, __FILE__, __LINE__, __func__,        \
              __VA_ARGS__);                                                    \
  } while (0)

#define ACT_WARN 0
#define ACT_STOP 1

#define CALL(ACT, FUNC, ...)                                                   \
  do {                                                                         \
    if (FUNC(__VA_ARGS__)) {                                                   \
      perror(#FUNC);                                                           \
      if (ACT == ACT_STOP)                                                     \
        exit(EXIT_FAILURE);                                                    \
    }                                                                          \
  } while (0)

#define abs(x) (((x) < 0) ? -(x) : (x))

struct job_descriptor {
  unsigned id;
  unsigned t;
  char name[50];
};

#define MSG_MAXSIZE 512
#define MSG_MAXNUM 10

int random_int(int below);
void my_mqopen(const char *var1, mqd_t *mqdes, int oflag);
void *memory_map(size_t len, char *name, int prot, int flags, int fd);
int my_shm_open(const char *name, int oflag, int mode);

#endif /* UTIL_H */