package bgu.spl.net.impl.BGRSServer.Messages;

public class COURSEREG extends MessageIn {

    private short courseNumber;
    private short numOfZeros;

    public COURSEREG() {
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
        this.courseNumber=courseNumber;
    }

    @Override
    public short getOpcode() {
        return 5;
    }
}
