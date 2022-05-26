package bgu.spl.net.impl.BGRSServer.Messages;

public class ISREGISTERED extends MessageIn {

    private int numOfZeros;
    private short courseNumber;

    public ISREGISTERED(){
        numOfZeros=0;
        courseNumber=-1;
    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    public void setCourseNumber(short courseNumber) {
        this.courseNumber = courseNumber;
    }

    public short getCourseNumber() {
        return courseNumber;
    }


    @Override
    public short getOpcode() {
        return 9;
    }
}
