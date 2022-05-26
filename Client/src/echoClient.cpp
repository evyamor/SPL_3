#include <stdlib.h>
#include <connectionHandler.h>
#include <thread>
#include <iostream>
#include <string>
#include <mutex>
#include <condition_variable>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
std::mutex mutex;
std::condition_variable cv;

bool logged_out=false;
bool logged_verify=false;
bool logged_final_certification=false;
bool active=true;


void keyboard(ConnectionHandler *connectionHandler) { // a thread to handle communication via keyboard
    while (active) {             // run "forever" ( as long as the program runs )since client should always have the option to write in keyboard to the server
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            if (!connectionHandler->sendLine(line)) {                   // if we fail to send line to server
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }
            if(line == "LOGOUT"){
                //wait for socket to decrypt answer
              std::unique_lock<std::mutex> unq_lock(mutex);
              cv.wait(unq_lock,[]{ return logged_out;});
                // if received ack,we will end process of keyboard
                if(logged_final_certification) {
                    active = false;
                }
                logged_verify=true;            // tells to client he can continue we identified
                // we unlock keyboard and notify even if we get error so keyboard an continue upon error
              unq_lock.unlock();
              cv.notify_one();
            }
        }
}

int main(int argc, char *argv[]) { // acts as a thread to handle communication via socket
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::thread keyboard_Thread(keyboard, &connectionHandler);

    bool kb_active=true;
    while (kb_active) { // thread for socket
        std::string answer;
        if (!connectionHandler.getLine(
                answer)) {               // false only if receiving a certain message from socket has failed
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            kb_active=false; // when we disconnect we terminate main and keyboard
        }
        if (answer == "bye") {                   // upon receiving ACK 4 ( acknowledgment for logout ) answer will be bye
            {
                std::lock_guard <std::mutex> unq_lock_guard1(mutex);
                logged_out = true;
            }
            cv.notify_one(); // tells keyboard to stop waiting and thus can terminate
            // wait for keyboard
            {
                logged_final_certification=true; // terminates keyboard
                std::unique_lock <std::mutex> unq_lock(mutex);
                cv.wait(unq_lock, [] { return logged_verify; });
                keyboard_Thread.detach();   // once done waiting for keyboard we can detach it
            }
            kb_active = false;
        }
        if( answer == "bad bye"){ // we let keyboard keep running because received an error for logout
            {
                std::lock_guard<std::mutex> unq_lock_guard2(mutex);
                logged_out=true;
            }
            cv.notify_one();
        }
    }
    return 0; // terminates after breaking the while when receiving ack for logout
}




