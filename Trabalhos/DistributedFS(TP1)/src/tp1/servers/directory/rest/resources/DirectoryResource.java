package tp1.servers.directory.rest.resources;

import jakarta.inject.Singleton;

import java.util.List;

import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.ErrorCodeConverter;
import tp1.api.util.Discovery;
import tp1.servers.Resource;
import tp1.servers.directory.logic.DirectoryLogic;
import tp1.servers.files.rest.FilesServer;
import tp1.servers.users.rest.UsersServer;


@Singleton
public class DirectoryResource extends Resource implements RestDirectory {

    private final Directory directory;


    public DirectoryResource(Discovery discovery){
        directory = new DirectoryLogic(discovery, FilesServer.SERVICE, UsersServer.SERVICE, true);
   }


    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        return super.getResult(()->directory.writeFile(filename, data, userId, password), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public void deleteFile(String filename, String userId, String password){
        super.getResult(()->directory.deleteFile(filename, userId, password), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        super.getResult(()->directory.shareFile(filename, userId, userIdShare, password), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        super.getResult(()->directory.unshareFile(filename, userId, userIdShare, password), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        return super.getResult(()->directory.getFile(filename, userId, accUserId, password), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        return super.getResult(()->directory.lsFile(userId, password), ErrorCodeConverter::convertErrorCodeToHttpError);
    }

}
