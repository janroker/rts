#ifndef LAB5_QUEUE_H
#define LAB5_QUEUE_H

#include "util.h"
#include <pthread.h>
#include <stdbool.h>
#include <stdlib.h>

struct node {
  void *data;
  struct node *next;
};

struct queue {
  struct node volatile *tail;
  struct node volatile *head;
  pthread_mutex_t *mutex;
  pthread_cond_t *cond;
  volatile unsigned N_sum;
  volatile unsigned M_sum;
};

void enqueue(struct queue *queue, struct job_descriptor *data);
struct job_descriptor *dequeue(struct queue *queue, bool should_wait,
                               bool (*wait_cond)(void));

#endif // LAB5_QUEUE_H
