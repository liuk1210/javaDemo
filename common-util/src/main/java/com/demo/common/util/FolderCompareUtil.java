package com.demo.common.util;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class FolderCompareUtil {

    public static void main(String[] args) {
        compareSourcesAndBackupFolder("C:\\Workspaces", "U:\\Workspaces");
    }

    public static void compareSourcesAndBackupFolder(String sourcesFolder, String backupFolder) {
        log.info("正在比对源文件夹（{}）与备份文件夹（{}）之间的文件差异...", sourcesFolder, backupFolder);
        log.info("正在读取源文件中...");
        JSONObject sources = calcDirectoryAllFileSha256(sourcesFolder);
        log.info("源文件夹（{}）所有文件信息读取完毕，已读取{}个文件，正在读取备份文件夹文件信息中...", sourcesFolder, sources.size());
        JSONObject backup = calcDirectoryAllFileSha256(backupFolder);
        log.info("备份文件夹（{}）所有文件信息读取完毕，已读取{}个文件，正在比对文件差异中...", backupFolder, backup.size());
        int size = compareJsonObjects(sources, backup, sourcesFolder, backupFolder);
        if (size > 0) {
            log.info("源文件与备份文件hash256比对完毕，存在以上{}个差异。", size);
        } else {
            log.info("源文件与备份文件hash256比对完毕，不存在任何差异。");
        }
    }

    public static int compareJsonObjects(JSONObject sources, JSONObject backup, String sourcesFolder, String backupFolder) {
        int size = 0;
        StringBuilder notEquals = new StringBuilder();
        StringBuilder backupNotExists = new StringBuilder();
        StringBuilder sourcesNotExists = new StringBuilder();
        for (String key : sources.keySet()) {
            if (!backup.containsKey(key)) {
                backupNotExists.append(backupFolder).append(key).append("\n");
                size++;
                continue;
            }
            if (!sources.get(key).equals(backup.get(key))) {
                notEquals.append(backupFolder).append(key).append("\n");
                size++;
            }
        }
        for (String key : backup.keySet()) {
            if (!sources.containsKey(key)) {
                sourcesNotExists.append(sourcesFolder).append(key).append("\n");
                size++;
            }
        }
        if (!notEquals.isEmpty()) {
            log.error("以下文件与源文件内容不一致：\n{}", notEquals.substring(0, notEquals.length() - 1));
        }
        if (!backupNotExists.isEmpty()) {
            log.error("以下文件在备份文件夹（{}）中不存在：\n{}", backupFolder, backupNotExists.substring(0, backupNotExists.length() - 1));
        }
        if (!sourcesNotExists.isEmpty()) {
            log.error("以下文件在源文件夹（{}）中不存在：\n{}", sourcesFolder, sourcesNotExists.substring(0, sourcesNotExists.length() - 1));
        }
        return size;
    }

    private static JSONObject calcDirectoryAllFileSha256(String directoryPath) {
        List<String> filePathList = new ArrayList<>();
        File directory = new File(directoryPath);
        listFiles(directory, filePathList);
        CompletableFuture<?>[] futures = Lists.partition(filePathList, 100).stream().map(subList -> CompletableFuture.supplyAsync(
                () -> {
                    JSONObject obj = new JSONObject();
                    for (String path : subList) {
                        String key = path.substring(directoryPath.length());
                        obj.put(key, calculateSHA256(path));
                    }
                    return obj;
                }
        )).toArray(CompletableFuture[]::new);
        //汇总所有任务执行结果
        return CompletableFuture.allOf(futures).thenApply(a -> {
            JSONObject rs = new JSONObject();
            for (CompletableFuture<?> future : futures) {
                Object futureJoin = future.join();
                if (futureJoin != null) {
                    if (futureJoin instanceof JSONObject m) {
                        rs.putAll(m);
                    }
                }
            }
            return rs;
        }).join();
    }


    public static void listFiles(File directory, List<String> filePathList) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory.");
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    listFiles(file, filePathList);
                } else {
                    filePathList.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static String calculateSHA256(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (FileInputStream fis = new FileInputStream(filePath)) {
                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            return bigInt.toString(16);
        } catch (Exception e) {
            throw new RuntimeException("计算hash256失败");
        }
    }

}
