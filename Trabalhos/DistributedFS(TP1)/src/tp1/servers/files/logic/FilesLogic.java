package tp1.servers.files.logic;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import tp1.api.service.util.Files;
import tp1.api.service.util.Result;


public class FilesLogic implements Files {

    private static final Logger Log = Logger.getLogger(FilesLogic.class.getName());


    public FilesLogic(){
    }


    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        Log.info("Writing file with id: " + fileId);

        if(fileId == null || data == null){
            Log.info("fileId or data invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Path path = Paths.get(".", fileId);
        try{
            java.nio.file.Files.write(path, data);
            return Result.ok();
        } catch (IOException e) {
            Log.info("Unable to write file.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        Log.info("Deleting file with id: " + fileId);

        Path path = Paths.get(".", fileId);
        Result.ErrorCode errorCode = this.checkIfFileExists(fileId, path);
        if(errorCode != Result.ErrorCode.OK)
            return Result.error(errorCode);

        try{
            java.nio.file.Files.delete(path);
        } catch (IOException e) {
            Log.info("Unable to delete file.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        Log.info("Writing file with id: " + fileId);

        if(fileId == null){
            Log.info("fileId invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Path path = Paths.get(".", fileId);
        Result.ErrorCode errorCode = this.checkIfFileExists(fileId, path);
        if(errorCode != Result.ErrorCode.OK)
            return Result.error(errorCode);

        try{
            return Result.ok(java.nio.file.Files.readAllBytes(path));
        } catch (IOException e) {
            Log.info("Unable to read file.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }

    private Result.ErrorCode checkIfFileExists(String fileId, Path path){
        if(fileId == null){
            Log.info("fileId is null.");
            return Result.ErrorCode.BAD_REQUEST;
        }

        if(java.nio.file.Files.notExists(path)){
            Log.info("File does not exist.");
            return Result.ErrorCode.NOT_FOUND;
        }
        return Result.ErrorCode.OK;
    }

}
