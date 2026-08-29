package tp1.servers.directory.logic;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.api.util.Discovery;
import tp1.api.util.URLConfigurator;
import tp1.clients.file.FilesClientFactory;
import tp1.clients.user.UsersClientFactory;


public class DirectoryLogic implements Directory {

    private static class FileServer implements Comparable<FileServer>{

        private final URI serverURI;
        private int numBytesWritten;


        FileServer(URI serverURI, int numBytesWritten){
            this.serverURI = serverURI;
            this.numBytesWritten = numBytesWritten;
        }


        public URI getServerURI() {
            return serverURI;
        }

        public int getNumBytesWritten() {
            return numBytesWritten;
        }

        public void incNumBytesWritten(int numBytesWritten) {
            this.numBytesWritten += numBytesWritten;
        }

        public void decNumBytesWritten(int numBytesWritten){
            this.numBytesWritten -= numBytesWritten;
        }

        @Override
        public int compareTo(FileServer fileServer) {
            return Integer.compare(this.getNumBytesWritten(), fileServer.getNumBytesWritten());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileServer that = (FileServer) o;
            return Objects.equals(serverURI, that.serverURI);
        }

    }

    private static class FileInfoBytes{

        private final FileInfo fileInfo;
        private int numBytes;


        private FileInfoBytes(FileInfo fileInfo, int numBytes) {
            this.fileInfo = fileInfo;
            this.numBytes = numBytes;
        }


        public FileInfo getFileInfo() {
            return fileInfo;
        }

        public int getNumBytes() {
            return numBytes;
        }

        public void setNumBytes(int numBytes) {
            this.numBytes = numBytes;
        }
    }

    private static final Logger Log = Logger.getLogger(DirectoryLogic.class.getName());

    private final Map<String, FileInfoBytes> fileInfoByFileId;
    private final Map<String, Set<FileInfo>> userFiles;
    private final List<FileServer> fileServers;

    private final Discovery discovery;

    private final String fileServiceName, usersServiceName;

    private final boolean isRest;

    private final UsersClientFactory usersClientFactory;
    private final FilesClientFactory filesClientFactory;


    public DirectoryLogic(Discovery discovery, String fileServiceName, String usersServiceName, boolean isRest){

        fileInfoByFileId = new HashMap<>();
        userFiles = new HashMap<>();
        fileServers = new LinkedList<>();

        this.discovery = discovery;

        this.fileServiceName = fileServiceName;
        this.usersServiceName = usersServiceName;

        this.isRest = isRest;

        usersClientFactory = new UsersClientFactory();
        filesClientFactory = new FilesClientFactory();
    }


    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        Log.info("Writing file.");

        // Check if provided data is valid
        if(filename == null || data == null || userId == null){
            Log.info("Bad request.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user with userId exists
        Result<User> userResult = this.getUsersService().getUser(userId, password);
        if(!userResult.isOK())
            return Result.error(userResult.error());

        User user = userResult.value();

        String fileIdForDirectoryService = this.getFileIdForDirectoryService(userId, filename);
        String fileIdForFilesService = this.getFileIdForFilesService(userId, filename);

        URI[] filesServiceURIs = URLConfigurator.getURLFor(discovery, fileServiceName);

        this.buildFileServersSortedSet(filesServiceURIs);

        FileInfoBytes fileInfoBytes;
        String oldFileURL = "";
        synchronized (fileInfoByFileId){
            fileInfoBytes = fileInfoByFileId.get(fileIdForDirectoryService);
            if(fileInfoBytes != null){
                oldFileURL = fileInfoBytes.getFileInfo().getFileURL();
                URI fileURI = URI.create(oldFileURL);
                String filesServiceURI = this.getServerUriFrom(fileURI);

                synchronized (fileServers){
                    int fileServerPos = this.fileServers.indexOf(new FileServer(URI.create(filesServiceURI), 0));
                    this.fileServers.get(fileServerPos).decNumBytesWritten(fileInfoBytes.getNumBytes());
                    Collections.sort(this.fileServers);
                }
            }
        }

        FileServer filesServer = null;
        Result<Void> fileWriteResult = null;
        var fileServersIt = this.fileServers.iterator();
        while(fileServersIt.hasNext() && fileWriteResult == null){
            filesServer = fileServersIt.next();
            Files filesService = filesClientFactory.getClient(filesServer.getServerURI());
            fileWriteResult = filesService.writeFile(fileIdForFilesService, data, null);
        }

        String fileURL = filesServer.getServerURI().toString() + "/files/" + fileIdForFilesService;
        boolean wasOverWrite = !oldFileURL.isEmpty() && !oldFileURL.equals(fileURL);

        if(fileWriteResult.isOK()){
            filesServer.incNumBytesWritten(data.length);
            Collections.sort(this.fileServers);

            Result<Void> fileDeleteResult = null;
            if(wasOverWrite){
                String serverURI = this.getServerUriFrom(URI.create(fileInfoBytes.getFileInfo().getFileURL()));
                Files filesService = filesClientFactory.getClient(URI.create(serverURI));
                fileDeleteResult = filesService.deleteFile(fileIdForFilesService, null);
            }

            if(wasOverWrite && !fileDeleteResult.isOK()){
                return Result.error(fileDeleteResult.error());
            }

            synchronized (fileInfoByFileId) {
                if (fileInfoBytes == null) {
                    fileInfoBytes = new FileInfoBytes(new FileInfo(user.getUserId(), filename, fileURL, new HashSet<>()), data.length);
                    fileInfoByFileId.put(fileIdForDirectoryService, fileInfoBytes);
                } else {
                    fileInfoBytes.getFileInfo().setFileURL(fileURL);
                    fileInfoBytes.setNumBytes(data.length);
                }
            }

            synchronized (userFiles){
                Set<FileInfo> fileInfoSet = userFiles.computeIfAbsent(userId, k -> new HashSet<>());
                fileInfoSet.add(fileInfoBytes.getFileInfo());
            }

            return Result.ok(fileInfoBytes.getFileInfo());
        } else {
            Log.info("Could not write file.");
            return Result.error(fileWriteResult.error());
        }

    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        Log.info("Deleting file.");

        // Check if provided data is valid.
        if(filename == null || userId == null ){
            Log.info("Bad request.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user with userId exists
        Result<User> userResult = this.getUsersService().getUser(userId, password);
        if(!userResult.isOK())
            return Result.error(userResult.error());

        String fileIdForDirectoryService = this.getFileIdForDirectoryService(userId, filename);
        String fileIdForFilesService = this.getFileIdForFilesService(userId, filename);

        synchronized (fileInfoByFileId) {
            synchronized (userFiles) {
                FileInfoBytes fileInfoBytes = fileInfoByFileId.get(fileIdForDirectoryService);

                if (fileInfoBytes == null) {
                    Log.info("File does not exist.");
                    return Result.error(Result.ErrorCode.NOT_FOUND);
                }
                FileInfo fileInfo = fileInfoBytes.getFileInfo();
                URI fileURI = URI.create(fileInfo.getFileURL());
                String filesServiceURI = this.getServerUriFrom(fileURI);
                Files filesService = filesClientFactory.getClient(URI.create(filesServiceURI));
                var fileResult = filesService.deleteFile(fileIdForFilesService, null);

                // Check if operation was successful
                if (!fileResult.isOK()) {
                    Log.info("Bad request.");
                    return Result.error(fileResult.error());
                }

                int numBytesWritten = fileInfoByFileId.remove(fileIdForDirectoryService).getNumBytes();
                fileServers.get(fileServers.indexOf(new FileServer(URI.create(filesServiceURI), 0))).decNumBytesWritten(numBytesWritten);
                Collections.sort(fileServers);
                var sharedUsers = fileInfo.getSharedWith();

                for (String u : sharedUsers) {
                    userFiles.get(u).remove(fileInfo);
                }
                userFiles.get(userId).remove(fileInfo);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("Sharing file.");

        // Check if provided data is valid
        if(filename == null || userId == null || userIdShare == null ){
            Log.info("Bad request.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Users usersService = this.getUsersService();

        // Check if users with userId and userIdShare exists
        Result<User> userResult = usersService.getUser(userId, password);
        Result<User> userShareResult = usersService.getUser(userIdShare, null);

        String fileIdForDirectoryService = this.getFileIdForDirectoryService(userId, filename);

        synchronized (fileInfoByFileId) {
            synchronized (userFiles) {
                FileInfoBytes fileInfoBytes = fileInfoByFileId.get(fileIdForDirectoryService);

                Result.ErrorCode errorCode = this.checkIfUsersAndFileExists(userResult, userShareResult, fileInfoBytes);
                if(errorCode != Result.ErrorCode.OK)
                    return Result.error(errorCode);

                FileInfo fileInfo = fileInfoBytes.getFileInfo();
                if (!userId.equals(userIdShare)) {
                    Set<String> sharedWith = fileInfo.getSharedWith();

                    sharedWith.add(userIdShare);

                    Set<FileInfo> fileInfoSet = userFiles.computeIfAbsent(userIdShare, k -> new HashSet<>());
                    fileInfoSet.add(fileInfo);
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        Log.info("Unsharing file.");

        // Check if provided data is valid
        if(filename == null || userId == null || userIdShare == null){
            Log.info("Bad request.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Users usersService = this.getUsersService();

        // Check if users with userId and userIdShare exists
        Result<User> userResult = usersService.getUser(userId, password);
        Result<User> userShareResult = usersService.getUser(userIdShare, null);

        String fileIdForDirectoryService = this.getFileIdForDirectoryService(userId, filename);

        synchronized (fileInfoByFileId) {
            synchronized (userFiles) {
                FileInfoBytes fileInfoBytes = fileInfoByFileId.get(fileIdForDirectoryService);

                Result.ErrorCode errorCode = this.checkIfUsersAndFileExists(userResult, userShareResult, fileInfoBytes);
                if(errorCode != Result.ErrorCode.OK)
                    return Result.error(errorCode);

                FileInfo fileInfo = fileInfoBytes.getFileInfo();
                if (!userId.equals(userIdShare)) {
                    Set<String> sharedWith = fileInfo.getSharedWith();
                    sharedWith.remove(userIdShare);

                    var files = userFiles.get(userIdShare);
                    files.remove(fileInfo);
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {

        // Check if provided data is valid
        if(filename == null || userId == null || accUserId == null){
            Log.info("Bad request.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Users usersService = this.getUsersService();

        // Check if users with userId and accUserId exists
        Result<User> ownerResult = usersService.getUser(userId, null);
        Result<User> accUserResult = usersService.getUser(accUserId, password);

        String fileIdForDirectoryService = this.getFileIdForDirectoryService(userId, filename);
        String fileIdForFilesService = this.getFileIdForFilesService(userId, filename);
        String fileUrl;

        synchronized (fileInfoByFileId) {
            FileInfoBytes fileInfoBytes = fileInfoByFileId.get(fileIdForDirectoryService);

            Result.ErrorCode errorCode = this.checkIfUsersAndFileExists(accUserResult, ownerResult, fileInfoBytes);
            if(errorCode != Result.ErrorCode.OK)
                return Result.error(errorCode);
            FileInfo fileInfo = fileInfoBytes.getFileInfo();

            // Check if the user has access to the file
            if ((!fileInfo.getOwner().equals(accUserId) && !fileInfo.getSharedWith().contains(accUserId))) {
                Log.info("Unauthorized access.");
                return Result.error(Result.ErrorCode.FORBIDDEN);
            }

            fileUrl = fileInfo.getFileURL();

            // For REST server
            if(isRest)
                throw new WebApplicationException(Response.temporaryRedirect(URI.create(fileUrl)).build());
        }

        // For SOAP server
        URI fileURI = URI.create(fileUrl);
        String filesServiceURI = this.getServerUriFrom(fileURI);
        Files filesService = filesClientFactory.getClient(URI.create(filesServiceURI));

        Result<byte[]> fileResult = filesService.getFile(fileIdForFilesService, null);

        if(fileResult.isOK())
            return Result.ok(fileResult.value());
        else
            return Result.error(fileResult.error());
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {

        // Check if provided data is valid
        if(userId == null){
            Log.info("Bad request.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if users with userId exists
        Result<User> userResult = this.getUsersService().getUser(userId, password);

        if(!userResult.isOK())
            return Result.error(userResult.error());

        synchronized (userFiles) {
            var files = userFiles.get(userId);

            List<FileInfo> fileList = new LinkedList<>();
            if (files != null)
                fileList.addAll(files);

            return Result.ok(fileList);
        }
    }

    private void buildFileServersSortedSet(URI[] fileServersURIs){
        synchronized (fileServers){
            int size = this.fileServers.size();
            if(fileServersURIs.length > size){
                for(URI uri : fileServersURIs){
                    var fileServer = new FileServer(uri, 0);
                    if(!this.fileServers.contains(fileServer))
                        fileServers.add(fileServer);
                }
            }
            if(this.fileServers.size() > size)
                Collections.sort(fileServers);
        }
    }

    private String getServerUriFrom(URI fileURI){
        return fileURI.getScheme() + "://" + fileURI.getAuthority() + "/" + fileURI.getPath().split("/")[1];
    }

    private String getFileIdForDirectoryService (String userId, String filename){
        return Paths.get(userId, filename).toString();
    }

    private String getFileIdForFilesService (String userId, String filename){
        return userId.replace(".", "_") + "_" + filename;
    }

    private Users getUsersService(){
        String usersServiceURI = URLConfigurator.getURLFor(discovery, usersServiceName)[0].toString();
        return usersClientFactory.getClient(URI.create(usersServiceURI));
    }

    private Result.ErrorCode checkIfUsersAndFileExists(Result<User> user1Result, Result<User> user2Result, FileInfoBytes fileInfoBytes){
        // Check if userId and userIdShare and file exists
        if (user1Result.error() == Result.ErrorCode.NOT_FOUND || user2Result.error() == Result.ErrorCode.NOT_FOUND || fileInfoBytes == null) {
            Log.info("Owner or user or file to share does not exist.");
            return Result.ErrorCode.NOT_FOUND;
        }

        // Check if password is incorrect
        if (user1Result.error() == Result.ErrorCode.FORBIDDEN) {
            Log.info("Wrong password.");
            return Result.ErrorCode.FORBIDDEN;
        }
        return Result.ErrorCode.OK;
    }

}