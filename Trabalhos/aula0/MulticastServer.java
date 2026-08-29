package aula0;

import java.io.* ;
import java.net.* ;

public class MulticastServer {
    
    public static void main(String[] args) throws Exception {

        final InetAddress address = InetAddress.getByName( args[0] ) ;
        if( ! address.isMulticastAddress()) {
        	System.out.println( "Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
        	System.exit( 1);
        }
        MulticastSocket socket = new MulticastSocket( 9000 ) ;
        socket.joinGroup( address);
        while( true ) {
            byte[] buffer = new byte[65536] ;
            DatagramPacket packet = new DatagramPacket( buffer, buffer.length ) ;
            socket.receive( packet ) ;
            System.out.write( packet.getData(), 0, packet.getLength() ) ;
        }
    }    
}
