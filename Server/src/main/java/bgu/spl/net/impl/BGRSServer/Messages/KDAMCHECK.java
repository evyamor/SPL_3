package bgu.spl.net.impl.BGRSServer.Messages;

public class KDAMCHECK extends MessageIn {

    private int numOfZeros;
    private short courseNumber;

    public KDAMCHECK(){
        numOfZeros=0;
        courseNumber=-1;
    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    public short getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(short courseNumber) {
        this.courseNumber = courseNumber;
    }

    @Override
    public short getOpcode() {
        return 6;
    }

}
