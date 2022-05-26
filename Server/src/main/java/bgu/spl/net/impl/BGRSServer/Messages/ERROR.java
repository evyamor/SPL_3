package bgu.spl.net.impl.BGRSServer.Messages;

public class ERROR extends MessageOut {

    private short messageOpCode;

    public ERROR(short messageOpCode) {
        this.messageOpCode=messageOpCode;
    }

    public short getMessageOpCode(){
        return messageOpCode;
    }
    @Override
    public short getOpcode() {
        return 13;
    }
}
