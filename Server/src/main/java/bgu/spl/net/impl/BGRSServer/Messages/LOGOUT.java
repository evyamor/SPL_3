package bgu.spl.net.impl.BGRSServer.Messages;

public class LOGOUT extends MessageIn {

    private int numOfZeros;

    public LOGOUT(){
        numOfZeros=0;
    }
    public int getNumOfZeros() {
        return numOfZeros;
    }

    @Override
    public short getOpcode() {
        return 4;
    }
}
