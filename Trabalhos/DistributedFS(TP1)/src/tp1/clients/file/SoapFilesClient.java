package tp1.clients.file;

import jakarta.xml.ws.BindingProvider;
import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.util.ErrorCodeConverter;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.SoapClient;

import java.net.URI;


public class SoapFilesClient extends SoapClient implements Files {

    private final SoapFiles filesProvider;


    public SoapFilesClient(URI serverURI) {
        super(serverURI, SoapFiles.NAMESPACE, SoapFiles.NAME);
        filesProvider = service.getPort(tp1.api.service.soap.SoapFiles.class);
        SoapClient.setClientTimeouts((BindingProvider) filesProvider);
    }


    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        return super.reTry( () -> cltWriteFile(fileId, data, token));
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        return super.reTry( () -> cltDeleteFile(fileId, token));
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        return super.reTry( () -> cltGetFile(fileId, token));
    }

    private Result<Void> cltWriteFile(String fileId, byte[] data, String token){
        try {
            filesProvider.writeFile(fileId, data, token);
            return Result.ok();
        } catch (FilesException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<Void> cltDeleteFile(String fileId, String token) {
        try {
            filesProvider.deleteFile(fileId, token);
            return Result.ok();
        } catch (FilesException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<byte[]> cltGetFile(String fileId, String token) {
        try {
            return Result.ok(filesProvider.getFile(fileId, token));
        } catch (FilesException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }
}
