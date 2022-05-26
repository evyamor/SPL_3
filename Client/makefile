CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread -Iboost_filesystem -Iboost_thread

all: bin/BGRSclient
	g++ -o bin/BGRSclient bin/connectionHandler.o bin/echoClient.o $(LDFLAGS) 

bin/BGRSclient: bin/connectionHandler.o bin/echoClient.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/socketClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp


.PHONY: clean
clean:
	rm -f bin/*
