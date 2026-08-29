package tp1.servers.files.soap;

import jakarta.jws.WebService;

import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.util.Files;
import tp1.servers.files.logic.FilesLogic;


@WebService(serviceName= SoapFiles.NAME, targetNamespace=SoapFiles.NAMESPACE, endpointInterface=SoapFiles.INTERFACE)
public class SoapFilesWebService extends tp1.servers.WebService implements SoapFiles {

    private final Files files;


    public SoapFilesWebService(){
        files = new FilesLogic();
    }


    @Override
    public byte[] getFile(String fileId, String token) throws FilesException {
        try {
            return super.getResult(()->files.getFile( fileId, token ));
        } catch (Exception e) {
            throw new FilesException(e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileId, String token) throws FilesException {
        try {
            super.getResult(()->files.deleteFile( fileId, token ));
        } catch (Exception e) {
            throw new FilesException(e.getMessage());
        }
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) throws FilesException {
        try {
            super.getResult(()->files.writeFile( fileId, data, token ));
        } catch (Exception e) {
            throw new FilesException(e.getMessage());
        }
    }

}
