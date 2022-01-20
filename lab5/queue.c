#include "queue.h"

void enqueue(struct queue *queue, struct job_descriptor *data) {
  CALL(ACT_STOP, pthread_mutex_lock, queue->mutex);
  struct node *newNode = (struct node *)malloc(sizeof(struct node));

  newNode->data = data;
  newNode->next = NULL;
  queue->N_sum += 1;
  queue->M_sum += data->t;

  if (queue->head == NULL) {
    queue->head = queue->tail = newNode;
  } else {
    queue->tail->next = newNode;
    queue->tail = newNode;
  }
  CALL(ACT_STOP, pthread_mutex_unlock, queue->mutex);
}

struct job_descriptor *dequeue(struct queue *queue, bool should_wait,
                               bool (*wait_cond)(void)) {
  CALL(ACT_STOP, pthread_mutex_lock, queue->mutex);
  while (wait_cond() && should_wait) {
    CALL(ACT_STOP, pthread_cond_wait, queue->cond, queue->mutex);
  }

  if (queue->head == NULL) {
    CALL(ACT_STOP, pthread_mutex_unlock, queue->mutex);
    return NULL;
  }

  struct job_descriptor *retval = queue->head->data;
  queue->N_sum -= 1;
  queue->M_sum -= retval->t;

  struct node *tmp = (struct node *)queue->head;
  if (queue->head == queue->tail) {
    queue->head = queue->tail = NULL;
  } else {
    queue->head = tmp->next;
  }

  free(tmp);
  CALL(ACT_STOP, pthread_mutex_unlock, queue->mutex);

  return retval;
}
