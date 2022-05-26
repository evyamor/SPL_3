package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.Messages.MessageEncoderDecoderImpl;
import bgu.spl.net.srv.Server;

import static java.lang.Integer.parseInt;

public class ReactorMain {
   public static void main(String[] args) {

        //Database dataBase= Database.getInstance();

        Server reactorServer= Server.reactor(parseInt(args[1]),parseInt(args[0]), BGRSProtocol::new, MessageEncoderDecoderImpl::new);
       reactorServer.serve();

    }
}
