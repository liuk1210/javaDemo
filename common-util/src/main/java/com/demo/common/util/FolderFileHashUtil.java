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

        // 打印每个文件的哈希值
        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {
                    String sha256 = getFileChecksum(file);
                    System.out.println("文件："+file.getName());
                    System.out.println("SHA256："+sha256+"\n");
                } catch (Exception e) {
                    log.error("Error computing hash for file: {}", file.getName());
                }
            }
        }
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

}
