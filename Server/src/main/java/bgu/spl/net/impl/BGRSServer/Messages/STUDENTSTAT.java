package bgu.spl.net.impl.BGRSServer.Messages;

public class STUDENTSTAT extends MessageIn
{
    private String userName;
    private int numOfZeros;

    public STUDENTSTAT() {
        numOfZeros=1;
        userName=null;
    }
    public int getNumOfZeros() {
        return numOfZeros;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userN) {
        userName = userN;
    }

    @Override
    public short getOpcode() {
        return 8;
    }
}
