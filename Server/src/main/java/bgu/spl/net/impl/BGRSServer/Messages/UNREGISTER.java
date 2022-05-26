package bgu.spl.net.impl.BGRSServer.Messages;

public class UNREGISTER extends MessageIn {

    private int numOfZeros;
    private short courseNumber;

    public UNREGISTER(){
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
        return 10;
    }
}
