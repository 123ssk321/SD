package tp1.servers.files.rest.resources;

import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.ErrorCodeConverter;
import tp1.servers.Resource;
import tp1.servers.files.logic.FilesLogic;
import tp1.api.service.util.Files;


public class FilesResource extends Resource implements RestFiles {

    private final Files files;


    public  FilesResource(){
        files = new FilesLogic();
    }


    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        super.getResult(()->files.writeFile(fileId, data, token), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public void deleteFile(String fileId, String token) {
        super.getResult(()->files.deleteFile(fileId, token), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public byte[] getFile(String fileId, String token) {
        return super.getResult(()->files.getFile(fileId, token), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

}
