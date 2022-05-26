package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.Messages.MessageEncoderDecoderImpl;
import bgu.spl.net.srv.Server;

import static java.lang.Integer.parseInt;

public class TPCMain {

   public static void main(String[] args)  {

        Server server = Server.threadPerClient(parseInt(args[0]), BGRSProtocol::new, MessageEncoderDecoderImpl::new);
        server.serve();
    }

}


