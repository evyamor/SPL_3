package bgu.spl.net.impl.echo;

import bgu.spl.net.impl.BGRSServer.Messages.ExampleMessage;
import bgu.spl.net.srv.Server;

public class EchoServerMain {

    public static void main(String[] args) {
        ExampleMessage em = new ExampleMessage();
        Server.threadPerClient(
                7777, //port
                EchoProtocol::new, //protocol factory
                LineMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                EchoProtocol::new, //protocol factory
//                LineMessageEncoderDecoder::new //message encoder decoder factory
//        ).serve();

    }
}
