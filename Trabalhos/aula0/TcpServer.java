package aula0;

import java.io.* ;
import java.net.* ;

public class TcpServer {
    
    public static void main(String[] args) throws Exception {
        
        ServerSocket ss = new ServerSocket( 9000 ) ;
        while( true ) {
            Socket cs = ss.accept() ;
            System.out.println("Accepted connection from client at: " + cs.getInetAddress().getHostName() + ":" + cs.getPort() ) ;
            
            InputStream is = cs.getInputStream() ;
            byte[] buffer = new byte[1024] ;
            int n ;
            while( (n = is.read( buffer )) != -1 ) {
                System.out.write( buffer, 0, n ) ;
            }
            cs.close() ;
            System.out.println("Connection closed.") ;
        }
    }
    
}
