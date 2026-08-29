package aula0;

import java.io.* ;
import java.net.* ;

public class MulticastClient {
    
    public static void main(String[] args) throws Exception {
        
        final int port = 9000 ;
        final InetAddress address = InetAddress.getByName( args[0] ) ;
        if( ! address.isMulticastAddress()) {
        	System.out.println( "Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
        }
        
        MulticastSocket socket = new MulticastSocket() ;
        String input ;
        do {
            input = readLine() + "\n";
            DatagramPacket packet = new DatagramPacket( input.getBytes(), input.getBytes().length ) ;
            packet.setAddress( address ) ;
            packet.setPort( port ) ;
            socket.send( packet ) ;

        } while( ! input.equals(".\n") ) ;
        
        socket.close() ;
    }
    
    
    public static String readLine() throws Exception {
        return new BufferedReader( new InputStreamReader( System.in ) ).readLine() ;
    }
}
