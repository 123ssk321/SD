package tp1.servers.directory.soap;

import jakarta.jws.WebService;

import java.util.List;

import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.util.Discovery;
import tp1.servers.directory.logic.DirectoryLogic;
import tp1.servers.files.soap.SoapFilesServer;
import tp1.servers.users.soap.SoapUsersServer;


@WebService(serviceName= SoapDirectory.NAME, targetNamespace=SoapDirectory.NAMESPACE, endpointInterface=SoapDirectory.INTERFACE)
public class SoapDirectoryWebService extends tp1.servers.WebService implements SoapDirectory {

    private final Directory directory;

    public SoapDirectoryWebService(Discovery discovery){
        directory = new DirectoryLogic(discovery, SoapFilesServer.SERVICE_NAME, SoapUsersServer.SERVICE_NAME, false);
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) throws DirectoryException {
        try {
            return super.getResult(()->directory.writeFile( filename, data, userId, password ));
        } catch (Exception e) {
            throw new DirectoryException(e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filename, String userId, String password) throws DirectoryException {
        try {
            super.getResult(()->directory.deleteFile( filename, userId, password ));
        } catch (Exception e) {
            throw new DirectoryException(e.getMessage());
        }
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
        try {
            super.getResult(()->directory.shareFile( filename, userId, userIdShare, password ));
        } catch (Exception e) {
            throw new DirectoryException(e.getMessage());
        }
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
        try {
            super.getResult(()->directory.unshareFile( filename, userId, userIdShare, password ));
        } catch (Exception e) {
            throw new DirectoryException(e.getMessage());
        }
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
        try {
            return super.getResult(()->directory.getFile( filename, userId, accUserId, password ));
        } catch (Exception e) {
            throw new DirectoryException(e.getMessage());
        }
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
        try {
            return super.getResult(()->directory.lsFile( userId, password ));
        } catch (Exception e) {
            throw new DirectoryException(e.getMessage());
        }
    }

}
