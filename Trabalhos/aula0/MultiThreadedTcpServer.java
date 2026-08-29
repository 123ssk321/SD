package aula0;

import java.io.* ;
import java.net.* ;

public class MultiThreadedTcpServer {
    
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket( 9000 ) ;
        while( true ) {
            new Thread( new ConnectionHandler( ss.accept() ) ).start() ;
        }
    }
}

class ConnectionHandler implements Runnable {
	private Socket cs ;

	ConnectionHandler( Socket cs ) 
	{
        this.cs = cs ;
    }
    
    public void run() {
        try {
            final int srcPort = cs.getPort() ;
            final InetAddress srcAddress = cs.getInetAddress() ;
            System.out.println("Accepted connection from : " +  srcAddress + ":" + srcPort ) ;
            
            int n ;
            byte[] buffer = new byte[1024] ;
            while( (n = cs.getInputStream().read( buffer ) ) != -1 ) {
                System.out.write( buffer, 0, n ) ;
            }
            cs.close() ;
        } catch( Exception x ) {
            x.printStackTrace() ;
        }
    }
}
