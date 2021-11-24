package org.zipExample;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Archiver {
    public static void main(String[] args) {
        Path source = null;
        Path zipFile = null;
        ArrayList<Path> fileList = new ArrayList<>();
        ArrayList<Path> dirList = new ArrayList<>();
        ArrayList<Path> zipFileList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            source = sourceFileSpecify(reader);
            zipFile = zipFileSpecify(reader);

            FileSearcher fileSearcher = new FileSearcher();
            Files.walkFileTree(source, fileSearcher);
            fileList.addAll(fileSearcher.getFileList());
            zipFileList.addAll(fileSearcher.getZipFileList());
            dirList.addAll(fileSearcher.getDirList());

            System.out.println("");
            System.out.println("Все папки:");
            for (Path p : dirList) {
                System.out.println(p.toString());
            }

            System.out.println("");
            System.out.println("Все файлы:");
            for (Path p : fileList) {
                System.out.println(p.toString());
            }

            System.out.println("");
            System.out.println("Все zip файлы:");
            for (Path p : zipFileList) {
                System.out.println(p.toString());
            }
            System.out.println("");

            archiveWholeDir(zipFile, fileList, dirList);
            Path unzipfolder = unzipFolderSpecify(reader);
            extractZip(zipFile, unzipfolder);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Конец!");
    }

    public static void archiveWholeDir(Path path, ArrayList<Path> fileList, ArrayList<Path> dirList) throws IOException {
        System.out.println("Начало архивирования.");
        Charset charset = Charset.forName("UTF-8");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path.toString()), charset)) {

            for (Path p : dirList) {
                ZipEntry entry = new ZipEntry(path.getParent().relativize(p.toAbsolutePath()).toString() + "/");
                zos.putNextEntry(entry);
                zos.closeEntry();
            }
            for (Path p : fileList) {
                ZipEntry entry = new ZipEntry(path.getParent().relativize(p.toAbsolutePath()).toString());
                zos.putNextEntry(entry);
                try (BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(p.toFile()))) {
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = fileReader.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
        }
        System.out.println("Архивирование закончено!");
    }

    public static void extractZip(Path pathToZip, Path destinationFolder) throws IOException {

        System.out.println("");
        System.out.println("Началась распаковка архива.");

        ZipFile file = new ZipFile(pathToZip.toFile());
        Enumeration entries = file.entries();
        List<ZipEntry> files = new ArrayList<>();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                File folder = new File(destinationFolder.toString() +
                        File.separator + changeSeparator(entry.getName()));
                if (!folder.exists()) {
                    folder.mkdirs();
                }
            } else {
                files.add(entry);
            }
        }
        for (ZipEntry entry : files) {
            try (InputStream is = file.getInputStream(entry);
                 FileOutputStream fos = new FileOutputStream(destinationFolder.toString() + File.separator + changeSeparator(entry.getName()))) {
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
        file.close();
        System.out.println("Распаковка архива закончена!");
        System.out.println("");
    }

    static String changeSeparator(String entryName) {
        int i;
        char[] chars = new char[entryName.length()];
        String newEntryName;

        for (i = 0; i < entryName.length(); i++) {
            if (entryName.charAt(i) == '/')
                chars[i] = File.separatorChar;
            else
                chars[i] = entryName.charAt(i);
        }
        newEntryName = new String(chars);
        return newEntryName;
    }

    public static Path sourceFileSpecify(BufferedReader reader) throws IOException {
        String sourceFilePath = null;
        Path source = null;
        while (true) {
            System.out.println("Укажите полный путь до файла или папки с файлами, которые необходимо заархивировать.");
            sourceFilePath = reader.readLine();
            try {
                source = Paths.get(sourceFilePath);
                if (!source.toFile().exists())
                    throw new FileNotFoundException();
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("Ресурс не существует по указанному адресу. Попробуйте еще раз.");
                continue;
            }
            break;
        }

        return source;
    }

    public static Path zipFileSpecify(BufferedReader reader) throws IOException {
        Path zipFile = null;
        while (true) {
            System.out.println("Укажите полный путь(включая расширение) где необходимо создать \".zip\" архив.");
            String zipFilePath = reader.readLine();
            if (!zipFilePath.endsWith(".zip")) {
                System.out.println("Указан неверный путь.\n" +
                        "Убедитесь, что указали имя файла и расширение \".zip\"\n" +
                        "Попробуйте еще раз.");
                continue;
            }
            zipFile = Paths.get(zipFilePath);
            break;
        }
        return zipFile;
    }

    public static Path unzipFolderSpecify(BufferedReader reader) throws IOException {
        Path unzipFolder = null;
        System.out.println("Укажите полный путь, по которому будет распакован только что созданный архив.");
        String unzipFolderPath = reader.readLine();
        try {
            unzipFolder = Paths.get(unzipFolderPath);
        }catch (InvalidPathException invalidPathException){
            System.out.println("Путь указа на верно. Попробуйте еще раз.");
        }
        return unzipFolder;
    }
}
