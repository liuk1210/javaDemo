package com.common.util.folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.common.util.sha256.Sha256Util.calculateSHA256;

public class FolderFileHashUtil {
    public static void main(String[] args) throws IOException {
        String folderPath = "C:\\Users\\Liuk\\Downloads\\soft";
        processFolder(folderPath);
    }

    public static void processFolder(String folderPath) throws IOException {
        Path path = Paths.get(folderPath);

        long totalFiles = countFiles(path);
        if (totalFiles == 0) {
            System.out.println("该文件夹下不存在文件！");
            return;
        }

        AtomicInteger processedFiles = new AtomicInteger(0);
        AtomicInteger createFileCount = new AtomicInteger(0);
        AtomicInteger existFileCount = new AtomicInteger(0);

        try (Stream<Path> walkStream = Files.walk(path)) {
            walkStream.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().endsWith(".sha256"))
                    .filter(p -> !p.getParent().endsWith("sha256"))
                    .forEach(filePath -> processFile(filePath, processedFiles, createFileCount, existFileCount, totalFiles));
        }

        System.out.printf("总文件数：%d ，已创建 %d 个sha256文件，已存在 %d 个sha256文件%n",
                totalFiles, createFileCount.get(), existFileCount.get());
    }

    private static long countFiles(Path path) throws IOException {
        try (Stream<Path> walkStream = Files.walk(path)) {
            return walkStream.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().endsWith(".sha256"))
                    .filter(p -> !p.getParent().endsWith("sha256"))
                    .count();
        }
    }

    private static void processFile(Path filePath, AtomicInteger processedFiles,
                                    AtomicInteger createFileCount, AtomicInteger existFileCount, long totalFiles) {
        try {
            int currentFile = processedFiles.incrementAndGet();
            int remaining = (int) totalFiles - currentFile;
            System.out.printf("当前是第 %d/%d 个文件，还剩余 %d 个%n", currentFile, totalFiles, remaining);

            System.out.println("正在读取 " + filePath.getFileName() + " 并计算sha256中...");
            String sha256 = calculateSHA256(filePath, true);
            System.out.println("\n文件：" + filePath.getFileName());
            System.out.println("SHA256：" + sha256);

            String sha256FileName = filePath.getFileName() + "." + sha256 + "." +
                    Files.size(filePath) + ".sha256";

            Path sha256FolderPath = filePath.getParent().resolve("sha256");
            Path sha256FilePath = sha256FolderPath.resolve(sha256FileName);

            boolean created = createFileWithDirectories(sha256FilePath.toString());
            if (created) {
                createFileCount.incrementAndGet();
            } else {
                existFileCount.incrementAndGet();
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("处理文件失败，文件路径为: " + filePath);
        }
    }

    /**
     * 创建一个文件及其必要的父目录
     *
     * @param absolutePath 文件的绝对路径
     * @return 如果文件被创建返回true，如果文件已存在返回false
     * @throws IOException 如果创建目录或文件时发生IO错误
     */
    public static boolean createFileWithDirectories(String absolutePath) throws IOException {
        Path path = Paths.get(absolutePath);
        Path parentDir = path.getParent();

        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
            System.out.println("文件已创建：" + path);
            return true;
        } else {
            System.out.println("文件已存在: " + path);
        }
        return false;
    }
}
