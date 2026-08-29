package aula0;

import java.io.* ;
import java.net.* ;

public class UdpServer {
    
    public static void main(String[] args) throws Exception {
        
        DatagramSocket socket = new DatagramSocket( 9000 ) ;
        while( true ) {
            byte[] buffer = new byte[65536] ;
            DatagramPacket packet = new DatagramPacket( buffer, buffer.length ) ;
            socket.receive( packet ) ;
            System.out.println( "Message from : " + packet.getAddress() + ":" + packet.getPort() ) ;
            System.out.write( packet.getData(), 0, packet.getLength() ) ;
        }
    }    
}
