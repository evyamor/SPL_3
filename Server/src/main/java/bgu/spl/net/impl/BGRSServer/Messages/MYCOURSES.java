package bgu.spl.net.impl.BGRSServer.Messages;

public class MYCOURSES extends MessageIn {

    private int numOfZeros;
    public MYCOURSES(){
        numOfZeros=0;
    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    @Override
    public short getOpcode() {
        return 11;
    }
}
