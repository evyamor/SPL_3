#include <connectionHandler.h>
#include <boost/lexical_cast.hpp>
#include <boost/algorithm/string.hpp>
#include<sstream>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;


ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_),
                                                                messages{"ADMINREG", "STUDENTREG", "LOGIN", "LOGOUT",
                                                                         "COURSEREG", "KDAMCHECK", "COURSESTAT",
                                                                         "STUDENTSTAT", "ISREGISTERED", "UNREGISTER",
                                                                         "MYCOURSES", "ACK", "ERROR"} {}

ConnectionHandler::~ConnectionHandler() {
    close();
}

short bytesToShort(char *bytesArr) {                        //function given, convert char array to short
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}

void shortToBytes(short num, char *bytesArr) {               //function given, convert short to char array
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception &e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {
            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string &line) {
    return getFrameAscii(line, '\0'); // get encrypted message from server
}

bool ConnectionHandler::sendLine(std::string &line) {
    return sendFrameAscii(line, '\0');
}


bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) { //decrypts message from socket
    char ch;
    short opcode;
    unsigned short msgopcode;
    char buffer[2];
    // extracting opcode to buffer from message
    if (!getBytes(buffer, 2)) {
        return false;
    }
    opcode = bytesToShort(buffer);
    // extracting related message opcode to buffer from message
    if (!getBytes(buffer, 2)) {
        return false;
    }
    msgopcode = bytesToShort(buffer);
    //converting opcodes into strings
    string opcode_str = decode(opcode);               //OPCODE MEANING
    string msgopcode_str = decode(msgopcode);
    string optional_str = "";                         // Holds a potential empty string for optional data, if given
    if ((msgopcode) > 5 && (msgopcode < 12 )&& (msgopcode != 10) && (opcode != 13)) { // read optional data
        try {
            do {
                if (!getBytes(&ch, 1)) {
                    return false;
                }
                if (ch != '\0')
                    optional_str.append(1, ch);
            } while (delimiter != ch);
        } catch (std::exception &e) {
            std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
            return false;
        }
    }
    string opCodeNum = std::to_string(
            msgopcode);           // Convert the related message OPCode number to the same number as string
    frame = handleFrame(opcode_str, opCodeNum,
                        optional_str); // makes frame = bye or "" and prints all information included ( decrypt the message )
    return true;
}


bool ConnectionHandler::sendFrameAscii(const std::string &line,
                                       char delimiter) { // encrypts message from keyboard to a message designed for the server
    std::vector<char> bytes_vector;                                               // appends all data in bytes for the message
    char buffer[2];
    std::vector<string> data;                                             // vector holds string parts of the data : OPC, MsgOPC, Optional Data
    boost::split(data, line, boost::is_any_of(" "));
    // Encrypt opcode
    short opcode = encodeToOPC(data[0]);
    shortToBytes(opcode, buffer);
    bytes_vector.push_back(buffer[0]);
    bytes_vector.push_back(buffer[1]);
    // Registers or Login
    if (opcode <= 3) {
        string username = data[1];
        string password = data[2];
        for (unsigned int i = 0;
             i < username.length(); i++) {              // push the username-string char by char into the vector
            bytes_vector.push_back(username.at(i));
        }
        bytes_vector.push_back('\0');    // a mini delimiter for recognizing username ended
        for (unsigned int j = 0;
             j < password.length(); j++) {             // push the password-string char by char into the vector
            bytes_vector.push_back(password.at(j));
        }
        bytes_vector.push_back('\0');   // a mini delimiter for recognizing password ended


    } else {                                                           //other messages which contains only 1 more word
        if (opcode == 8) {                                               //STUDENTSTAT ( has a 0 byte in the end )
            for (unsigned int i = 0; i < data[1].length(); i++) {
                bytes_vector.push_back(data[1].at(i));
            }
            bytes_vector.push_back('\0');
        } else {
            if(opcode!=11 && opcode!=4) {           // for logout and coursestat do not contain optional data
                short msgOptionalDataShort = boost::lexical_cast<short>(data[1]); // covert string to short using boost
                shortToBytes(msgOptionalDataShort, buffer);
                bytes_vector.push_back(buffer[0]);
                bytes_vector.push_back(buffer[1]);
            }
        }
    }

    char bytes[bytes_vector.size()];         // a char* which contains the full message
    for (unsigned int i = 0; i < bytes_vector.size(); i++) {        // appending the char array using the bytes vector
        bytes[i] = bytes_vector[i];
    }
    bool result = sendBytes(bytes, bytes_vector.size()); // true if sending message was successful
    return result;
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

//Decodes the line from server
std::string
ConnectionHandler::handleFrame(std::string &lineOPC, std::string &lineMsgOPC,
                               std::string &lineMsgOptional) { // prints the message and returns bye if got ack for logout
    //Print
    if (lineMsgOptional.empty())                                      // message doesn't contain optional information
        std::cout << lineOPC + " " + lineMsgOPC << std::endl;
    else                                                            // message contains optional information
        std::cout << lineOPC + " " + lineMsgOPC + "\n" + lineMsgOptional << std::endl;
    //Check if got ACK for LOGOUT
    if (lineMsgOPC == "4" && lineOPC == "ACK") {
        return "bye";
    }
    if(lineMsgOPC == "4" && lineOPC == "ERROR") { // err for logout we dont want the keyboard to terminate
        return "bad bye";
    }

        return ""; // returns empty string
}

short
ConnectionHandler::encodeToOPC(const std::string &line) {                          // makes string to OP code in short
    short answer = 0;
    for (int i = 0; i < 13; i++) {
        if (line == messages[i]) {
            answer = i + 1;
            break;                                          // we want to break loop when specific message has been found
        }
    }
    return answer;
}

std::string ConnectionHandler::decode(short opcode_line) {      // makes  OP code to string
    return messages[opcode_line - 1];
}






