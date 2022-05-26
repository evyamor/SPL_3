package bgu.spl.net.impl.BGRSServer.Messages;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private byte[] opBytes = new byte[2];
    private short opCode;
    private Message message = null;
    private int zeroCounter = 0;
    private int pointer = 0;
    private String nextString = null;
    private LinkedList<String> strings;
    private boolean firstZero = true;
    private byte[] courseNum = new byte[2];
    // private byte[] courseNum1=new byte[1];
    boolean onlyOneByte;


    // -------------------- first 4 from spl site--------------------------

    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;

    }

    private byte[] shortToBytes(short num) {//turn short type to 2 bytes array
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private String popString() {//makes string out of bytes array
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        return result;
    }

    ////--------------------------------------------------------------
    @Override
    public byte[] encode(Message message) {
        short opCode = message.getOpcode();
        byte[] toReturn = new byte[4];//maybe null
        byte[] op;
        if (opCode == 12) {
            op = shortToBytes(opCode);
            byte[] opBytes = shortToBytes(((ACK) message).getMsgOpCode());
            String content = ((ACK) message).getOptional();
            byte[] optional;
            if (content != null) {
                optional = content.getBytes();
                toReturn = new byte[optional.length + 5];

                toReturn[0] = op[0];
                toReturn[1] = op[1];
                toReturn[2] = opBytes[0];
                toReturn[3] = opBytes[1];

                for (int i = 4; i < optional.length + 4; i++) {
                    toReturn[i] = optional[i - 4];
                }
                toReturn[toReturn.length - 1] = 0;
            } else {
                toReturn[0] = op[0];
                toReturn[1] = op[1];
                toReturn[2] = opBytes[0];
                toReturn[3] = opBytes[1];
            }
        }

        if (opCode == 13) {
            byte[] opBytes = shortToBytes(((ERROR) message).getMessageOpCode());
            op = shortToBytes(opCode);
            toReturn[0] = op[0];
            toReturn[1] = op[1];
            toReturn[2] = opBytes[0];
            toReturn[3] = opBytes[1];
        }
        return toReturn;
    }

    @Override

    public Message decodeNextByte(byte nextByte) {

        if (nextByte == 0 & !firstZero)//decrease the number of zeros
            zeroCounter--;

        if (firstZero)
            firstZero = false;

        if (pointer < 2) {//first 2 bytes go to the op-array
            opBytes[pointer] = nextByte;
            pointer++;

        }

        if (pointer > 2 & nextByte != 0) {//starts adding bytes to the Main array after knowing the opcode
            pushByte(nextByte);
        }


        if (pointer == 2)//after 2 bytes we can know the op code
        {
            opCode = bytesToShort(opBytes);
            pointer++;
        }

        switch (opCode) {
            case 1: {
                if (message == null) {//initialing the message king and its zeros amount
                    message = new ADMINREG();
                    zeroCounter = ((ADMINREG) message).getNumOfZeros();
                    strings = new LinkedList<>();
                }
                if (zeroCounter == 1 & nextByte == 0) {
                    nextString = popString();
                    strings.addLast(nextString);
                    clearBytes();
                    len = 0;
                }
                if (zeroCounter == 0) {
                    nextString = popString();
                    strings.addLast(nextString);
                    ADMINREG msg = new ADMINREG();
                    msg.setUsername(strings.pollFirst());
                    msg.setPassword(strings.pollFirst());
                    resetFields();
                    return msg;
                }
                break;
            }

            case 2: {
                if (message == null) {//initialing the message king and its zeros amount
                    message = new STUDENTREG();
                    zeroCounter = ((STUDENTREG) message).getNumOfZeros();
                    strings = new LinkedList<>();
                }
                if (zeroCounter == 1 & nextByte == 0) {
                    nextString = popString();
                    strings.addLast(nextString);
                    clearBytes();
                    len = 0;
                }
                if (zeroCounter == 0) {
                    nextString = popString();
                    strings.addLast(nextString);
                    STUDENTREG msg = new STUDENTREG();
                    msg.setUsername(strings.pollFirst());
                    msg.setPassword(strings.pollFirst());
                    resetFields();
                    return msg;
                }
                break;
            }
            case 3: {
                if (message == null) {
                    message = new LOGIN();
                    zeroCounter = ((LOGIN) message).getNumOfZeros();
                    strings = new LinkedList<>();
                }
                if (nextByte == 0) {
                    nextString = popString();
                    strings.addLast(nextString);
                    clearBytes();
                    len = 0;
                }
                if (zeroCounter == 0) {
                    LOGIN msg = new LOGIN();
                    msg.setUsername(strings.pollFirst());
                    msg.setPassword(strings.pollFirst());
                    resetFields();
                    return msg;
                }
                break;
            }
            case 4: {
                if (message == null) {
                    message = new LOGOUT();
                }
                LOGOUT msg = new LOGOUT();
                resetFields();
                return msg;
            }
            case 5: {
                if (message == null) {
                    message = new COURSEREG();
                }
                if (nextByte == 0) {
                    courseNum[0] = nextByte;
                    onlyOneByte = true;
                }
                if (nextByte != 0 & onlyOneByte) {
                    courseNum[1] = nextByte;
                    onlyOneByte = false;
                    COURSEREG msg = new COURSEREG();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    return msg;
                }
                if (len == 2) {
                    courseNum[0] = bytes[0];
                    courseNum[1] = bytes[1];
                    COURSEREG msg = new COURSEREG();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    return msg;
                }
                break;
            }
            case 6: {
                if (message == null) {
                    message = new KDAMCHECK();
                }
                if (nextByte == 0) {
                    courseNum[0] = nextByte;
                    onlyOneByte = true;
                }
                if (nextByte != 0 & onlyOneByte) {
                    courseNum[1] = nextByte;
                    KDAMCHECK msg = new KDAMCHECK();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    onlyOneByte = false;
                    return msg;
                }

                if (len == 2) {
                    courseNum[0] = bytes[0];
                    courseNum[1] = bytes[1];
                    KDAMCHECK msg = new KDAMCHECK();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    return msg;
                }
                break;
            }
            case 7: {
                if (message == null) {
                    message = new COURSESTAT();
                    clearBytes();
                    len = 0;
                }
                if (nextByte == 0) {
                    courseNum[0] = nextByte;
                    onlyOneByte = true;
                }
                if (nextByte != 0 & onlyOneByte) {
                    courseNum[1] = nextByte;
                    COURSESTAT msg = new COURSESTAT();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    onlyOneByte = false;
                    resetFields();
                    return msg;
                }
                if (len == 2) {
                    courseNum[0] = bytes[0];
                    courseNum[1] = bytes[1];
                    COURSESTAT msg = new COURSESTAT();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    return msg;
                }
                break;
            }
            case 8: {
                if (message == null) {
                    message = new STUDENTSTAT();
                    zeroCounter = ((STUDENTSTAT) message).getNumOfZeros();
                    strings = new LinkedList<>();
                }
                if (nextByte == 0) {
                    nextString = popString();
                    strings.addLast(nextString);
                    STUDENTSTAT msg = new STUDENTSTAT();
                    msg.setUserName(strings.pollFirst());
                    resetFields();
                    return msg;
                }
                break;
            }
            case 9: {
                if (message == null) {
                    message = new ISREGISTERED();
                    clearBytes();
                    len = 0;
                }
                if (nextByte == 0) {
                    courseNum[0] = nextByte;
                    onlyOneByte = true;
                }
                if (nextByte != 0 & onlyOneByte) {
                    courseNum[1] = nextByte;
                    ISREGISTERED msg = new ISREGISTERED();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    onlyOneByte = false;
                    return msg;
                }
                if (len == 2) {
                    courseNum[0] = bytes[0];
                    courseNum[1] = bytes[1];
                    ISREGISTERED msg = new ISREGISTERED();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    return msg;
                }

                break;
            }
            case 10: {
                if (message == null) {
                    message = new UNREGISTER();
                    clearBytes();
                    len = 0;
                }
                if (nextByte == 0) {
                    courseNum[0] = nextByte;
                    onlyOneByte = true;
                }
                if (nextByte != 0 & onlyOneByte) {
                    courseNum[1] = nextByte;
                    UNREGISTER msg = new UNREGISTER();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    onlyOneByte = false;
                    return msg;
                }

                if (len == 2) {
                    courseNum[0] = bytes[0];
                    courseNum[1] = bytes[1];
                    UNREGISTER msg = new UNREGISTER();
                    msg.setCourseNumber(bytesToShort(courseNum));
                    resetFields();
                    return msg;
                }
                break;
            }
            case 11:{
                if (message == null) {
                    message = new MYCOURSES();
                }
                MYCOURSES msg = new MYCOURSES();
                resetFields();
                return msg;
            }
            //need to return the message.
        }
        return null;
    }

    private void clearBytes() {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
    }

    private void resetFields() {
        pointer = 0;
        clearBytes();
        message = null;
        firstZero = true;
        len = 0;
        opCode=0;
    }

}

