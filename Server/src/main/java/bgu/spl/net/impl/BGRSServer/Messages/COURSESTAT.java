package bgu.spl.net.impl.BGRSServer.Messages;

public class COURSESTAT extends MessageIn {

    private short courseNumber;
    private int numOfZeros;

    public COURSESTAT(){
        courseNumber=-1;
        numOfZeros=0;
    }

    public short getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(short courseNumber) {
        this.courseNumber = courseNumber;
    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    @Override
    public short getOpcode() {
        return 7;
    }
}
