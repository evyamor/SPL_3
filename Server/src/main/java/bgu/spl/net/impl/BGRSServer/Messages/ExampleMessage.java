package bgu.spl.net.impl.BGRSServer.Messages;

public class ExampleMessage extends MessageIn {
    public ExampleMessage(){}

    @Override
    public short getOpcode() {
        return -15;
    }
}
