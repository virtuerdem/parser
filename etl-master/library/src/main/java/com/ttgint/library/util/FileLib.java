package com.ttgint.library.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileLib {

    @Value("${app.engine.rootPath}")
    String localRootPath;
    @Value("${app.engine.rawFolder}")
    String localRawFolder;
    @Value("${app.engine.ctlFolder}")
    String localCtlFolder;
    @Value("${app.engine.logFolder}")
    String localLogFolder;
    @Value("${app.engine.badFolder}")
    String localBadFolder;
    @Value("${app.engine.failedFolder}")
    String localFailedFolder;
    @Value("${app.engine.exportFolder}")
    String localExportFolder;

    @Value("${app.engine.mountedExportPath}")
    String mountedExportPath;

    public void createLocalPaths() {
        new File(localRootPath).mkdirs();
        new File((localRootPath + "/" + localRawFolder).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localCtlFolder).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localLogFolder).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localBadFolder).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localFailedFolder).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localExportFolder).replace("//", "/")).mkdirs();
    }

    public void createFlowPaths(String folderName) {
        new File((localRootPath + "/" + localRawFolder + "/" + folderName).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localCtlFolder + "/" + folderName).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localLogFolder + "/" + folderName).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localBadFolder + "/" + folderName).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localFailedFolder + "/" + folderName).replace("//", "/")).mkdirs();
        new File((localRootPath + "/" + localExportFolder + "/" + folderName).replace("//", "/")).mkdirs();
    }

    public String createRawPath(String folderName) {
        String path = (localRootPath + "/" + localRawFolder + "/" + folderName + "/").replace("//", "/");
        new File(path).mkdirs();
        return path;
    }

    public String getRawPath(String folderName) {
        return (localRootPath + "/" + localRawFolder + "/" + folderName + "/").replace("//", "/");
    }

    public String getCtlPath(String folderName) {
        return (localRootPath + "/" + localCtlFolder + "/" + folderName + "/").replace("//", "/");
    }

    public String getLogPath(String folderName) {
        return (localRootPath + "/" + localLogFolder + "/" + folderName + "/").replace("//", "/");
    }

    public String getBadPath(String folderName) {
        return (localRootPath + "/" + localBadFolder + "/" + folderName + "/").replace("//", "/");
    }

    public String getExportPath(String folderName) {
        return (localRootPath + "/" + localExportFolder + "/" + folderName + "/").replace("//", "/");
    }

    public List<File> readFilesInCurrentPath(String path) {
        return Arrays.stream(new File(path).listFiles())
                .filter(File::isFile)
                .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                .collect(Collectors.toList());
    }

    public List<File> readFilesInCurrentPathByPostfix(String path, String postfix) {
        return Arrays.stream(new File(path).listFiles())
                .filter(File::isFile)
                .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                .filter(e -> e.getName().endsWith(postfix))
                .collect(Collectors.toList());
    }

    public List<File> readFilesInCurrentPathByContains(String path, String content) {
        return Arrays.stream(new File(path).listFiles())
                .filter(File::isFile)
                .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                .filter(e -> e.getName().contains(content))
                .collect(Collectors.toList());
    }

    public List<File> readFilesInCurrentPathByEndWith(String path, String content) {
        return Arrays.stream(new File(path).listFiles())
                .filter(File::isFile)
                .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                .filter(e -> e.getName().endsWith(content))
                .collect(Collectors.toList());
    }

    public List<File> readFoldersInCurrentPath(String path) {
        return Arrays.stream(new File(path).listFiles())
                .filter(File::isDirectory)
                .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                .collect(Collectors.toList());
    }

    public List<File> readFilesInWalkingPath(String path) {
        List<File> files = new ArrayList<>(readFilesInCurrentPath(path));
        for (File folder : readFoldersInCurrentPath(path)) {
            files.addAll(readFilesInWalkingPath(folder.getPath()));
        }
        return files;
    }

    public List<File> readFilesInWalkingPathByPostfix(String path, String postfix) {
        List<File> files = new ArrayList<>(readFilesInCurrentPathByPostfix(path, postfix));
        for (File folder : readFoldersInCurrentPath(path)) {
            files.addAll(readFilesInCurrentPathByPostfix(folder.getPath(), postfix));
        }
        return files;
    }

    public List<File> readFilesInWalkingPathByContains(String path, String content) {
        List<File> files = new ArrayList<>(readFilesInCurrentPathByContains(path, content));
        for (File folder : readFoldersInCurrentPath(path)) {
            files.addAll(readFilesInCurrentPathByContains(folder.getPath(), content));
        }
        return files;
    }

    public List<File> readFoldersInWalkingPath(String path) {
        List<File> files = new ArrayList<>(readFoldersInCurrentPath(path));
        for (File folder : readFoldersInCurrentPath(path)) {
            files.addAll(readFoldersInWalkingPath(folder.getPath()));
        }
        return files;
    }

    public Boolean deleteFile(File file) {
        try {
            Files.delete(Path.of(file.getPath()));
            return true;
        } catch (Exception exception) {
        }
        return false;
    }

    public Boolean deleteFile(String path) {
        try {
            Files.delete(Paths.get(path));
            return true;
        } catch (Exception exception) {
        }
        return false;
    }

    public Boolean deleteFileIfExists(File file) {
        try {
            Files.deleteIfExists(Path.of(file.getPath()));
            return true;
        } catch (Exception exception) {
        }
        return false;
    }

    public Boolean deleteFileIfExists(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
            return true;
        } catch (Exception exception) {
        }
        return false;
    }

}
