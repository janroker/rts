CFLAGS = -Wall -Wextra -pedantic -g $(DEBUG)
CC = gcc
LDFLAGS = -lpthread -lrt

all: posluzitelj generator

POSLUZITELJ_O = util.o posluzitelj.o queue.o # cini se da moraju bit iznad cc bla bla jer se ne prevode kao dependency inace...
GENERATOR_O = generator.o util.o

posluzitelj: $(POSLUZITELJ_O)
	$(CC) -o $@ $(POSLUZITELJ_O) $(LDFLAGS)

generator: $(GENERATOR_O)
	$(CC) -o $@ $(GENERATOR_O) $(LDFLAGS)

clean:
	-rm -f posluzitelj generator *.o *.core

util.o: util.c util.h
queue.o: util.o queue.c queue.h
posluzitelj.o: util.o queue.o posluzitelj.c
generator.o: util.o generator.c
