package org.zipExample;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileSearcher extends SimpleFileVisitor<Path> {
    private ArrayList<Path> fileList = new ArrayList<>();
    private ArrayList<Path> dirList = new ArrayList<>();
    private ArrayList<Path> zipFileList = new ArrayList<>();

    public ArrayList<Path> getFileList() {
        return fileList;
    }

    public ArrayList<Path> getDirList() {
        return dirList;
    }

    public ArrayList<Path> getZipFileList() {
        return zipFileList;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        dirList.add(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        fileList.add(file);
        if (file.toString().endsWith(".zip"))
            zipFileList.add(file);
        return FileVisitResult.CONTINUE;
    }
}
