package com.demo.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class FolderFileHashUtil {
    public static void main(String[] args) {
        String folderPath = "C:\\Workspaces\\Software";

        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("No files found in the specified directory.");
            return;
        }

        // 计算最长的文件名长度
        int maxFileNameLength = 0;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().length() > maxFileNameLength) {
                maxFileNameLength = file.getName().length();
            }
        }

        // 打印表头
        System.out.printf("%-" + maxFileNameLength + "s\tSHA-256 Hash%n", "File Name");
        // 打印每个文件的哈希值
        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    String sha256 = getFileChecksum(file);
                    System.out.printf("%-" + maxFileNameLength + "s\t%s%n", file.getName(), sha256);
                } catch (Exception e) {
                    log.error("Error computing hash for file: {}", file.getName());
                }
            }
        }
    }

    private static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
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

}
