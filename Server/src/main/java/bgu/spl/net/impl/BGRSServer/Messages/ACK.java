package bgu.spl.net.impl.BGRSServer.Messages;

public class ACK extends MessageOut {

    private short msgOpCode;
    private String optional=null;
    private int numOfZeros;

    public ACK(short msgOpCode){
        this.msgOpCode=msgOpCode;
    }

    public short getMsgOpCode() {
        return msgOpCode;
    }
    public void setOptional(String s){
        optional= s;
    }
    public String getOptional(){
         return optional;
    }
    @Override
    public short getOpcode() {
        return 12;
    }
}
