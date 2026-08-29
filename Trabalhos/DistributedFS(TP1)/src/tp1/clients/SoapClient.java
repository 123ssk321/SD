package tp1.clients;

import com.sun.xml.ws.client.BindingProviderProperties;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.xml.namespace.QName;


public class SoapClient {

    private static final Logger Log = Logger.getLogger(SoapClient.class.getName());

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    protected static final int RETRY_SLEEP = 1000;
    protected static final int MAX_RETRIES = 3;

    final URI serverURI;

    protected Service service;


    public SoapClient(URI serverURI, String namespace, String serviceName){
        this.serverURI = serverURI;

        QName qname = new QName(namespace, serviceName);
        try {
            service = Service.create( URI.create(serverURI + "?wsdl").toURL(), qname);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    protected <T> T reTry(Supplier<T> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (WebServiceException we) {
                Log.fine("WebServiceException: " + we.getMessage());
                sleep();
            } catch (Exception x) {
                Log.fine("Exception: " + x.getMessage());
                x.printStackTrace();
                break;
            }
        return null;
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_SLEEP);
        } catch (InterruptedException x) { // nothing to do...
        }
    }

    public static void setClientTimeouts(BindingProvider port ){
        port.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        port.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
    }

}
