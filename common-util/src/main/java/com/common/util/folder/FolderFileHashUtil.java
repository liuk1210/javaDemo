package com.common.util.folder;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class FolderFileHashUtil {

    public static void main(String[] args) {
        String folderPath = "C:\\Users\\Liuk\\Downloads\\soft";
        String sha256FolderPath = folderPath + "\\" + "sha256";
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("该文件夹下不存在文件！");
            return;
        }

        int sum = 0;
        int createFileCount = 0;
        int existFileCount = 0;

        // 打印每个文件的哈希值
        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    System.out.println("正在读取" + file.getName() + "中...");
                    String sha256 = getFileChecksum(file);
                    System.out.println("文件：" + file.getName());
                    System.out.println("SHA256：" + sha256);
                    String sha256FileName = file.getName() + "." + sha256 + "." + file.length() + ".sha256";
                    boolean create = createFileWithDirectories(sha256FolderPath + "\\" + sha256FileName);
                    if (create) {
                        createFileCount++;
                    } else {
                        existFileCount++;
                    }
                    System.out.println();
                    sum++;
                } catch (Exception e) {
                    log.error("计算sha256并写入文件失败，文件名为: {}", file.getName());
                }
            }
        }

        System.out.println("总文件数：" + sum + "，已创建" + createFileCount + "个sha256文件，已存在" + existFileCount + "个sha256文件");
    }

    private static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 创建一个文件及其必要的父目录
     *
     * @param absolutePath 文件的绝对路径
     * @throws IOException 如果创建目录或文件时发生IO错误
     */
    public static boolean createFileWithDirectories(String absolutePath) throws IOException {
        // 将字符串路径转换为Path对象
        Path path = Paths.get(absolutePath);

        // 获取父目录路径
        Path parentDir = path.getParent();

        // 如果父目录不存在，则创建所有必需的父目录
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // 创建文件（如果需要）
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
