package tp1.clients.directory;

import jakarta.xml.ws.BindingProvider;

import java.net.URI;
import java.util.List;

import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.ErrorCodeConverter;
import tp1.api.service.util.Result;
import tp1.clients.SoapClient;


public class SoapDirectoryClient extends SoapClient implements Directory {

    private final SoapDirectory directoryProvider;


    public SoapDirectoryClient(URI serverURI) {
        super(serverURI, SoapDirectory.NAMESPACE, SoapDirectory.NAME);
        directoryProvider = service.getPort(tp1.api.service.soap.SoapDirectory.class);
        SoapClient.setClientTimeouts((BindingProvider) directoryProvider);
    }


    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        return super.reTry( () -> cltWriteFile(filename, data, userId, password));
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        return super.reTry( () -> cltDeleteFile(filename, userId, password));
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry( () -> cltShareFile(filename, userId, userIdShare, password));
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry( () -> cltUnshareFile(filename, userId, userIdShare, password));
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        return super.reTry( () -> cltGetFile(filename, userId, accUserId, password));
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        return super.reTry( () -> cltLsFile(userId, password));
    }

    private Result<FileInfo> cltWriteFile(String filename, byte[] data, String userId, String password) {
        try {
            return Result.ok(directoryProvider.writeFile(filename, data, userId, password));
        } catch (DirectoryException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<Void> cltDeleteFile(String filename, String userId, String password) {
        try {
            directoryProvider.deleteFile(filename, userId, password);
            return Result.ok();
        } catch (DirectoryException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<Void> cltShareFile(String filename, String userId, String userIdShare, String password) {
        try {
            directoryProvider.shareFile(filename, userId, userIdShare, password);
            return Result.ok();
        } catch (DirectoryException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<Void> cltUnshareFile(String filename, String userId, String userIdShare, String password) {
        try {
            directoryProvider.unshareFile(filename, userId, userIdShare, password);
            return Result.ok();
        } catch (DirectoryException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<byte[]> cltGetFile(String filename, String userId, String accUserId, String password) {
        try {
            return Result.ok(directoryProvider.getFile(filename, userId, accUserId, password));
        } catch (DirectoryException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<List<FileInfo>> cltLsFile(String userId, String password) {
        try {
            return Result.ok(directoryProvider.lsFile(userId, password));
        } catch (DirectoryException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }
}
