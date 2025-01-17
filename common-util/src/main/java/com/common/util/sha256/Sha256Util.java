package com.common.util.sha256;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

public class Sha256Util {

    public static String calculateSHA256(Path file) {
        return calculateSHA256(file, false);
    }

    public static String calculateSHA256(Path file, boolean printProgress) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 使用直接缓冲区
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024); // 1MB

            long fileSize = Files.size(file); // 获取文件总大小
            long totalRead = 0; // 已读取的字节数
            long lastUpdateTime = System.currentTimeMillis(); // 上次更新时间
            long updateInterval = 100; // 每 0.1 秒更新一次进度

            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                int bytesRead;
                while ((bytesRead = channel.read(buffer)) > 0) {
                    buffer.flip(); // 切换为读模式
                    digest.update(buffer); // 直接更新 MessageDigest
                    buffer.clear(); // 清空缓冲区

                    if (printProgress) {
                        long currentTime = System.currentTimeMillis();
                        totalRead += bytesRead; // 更新已读取的字节数
                        // 检查是否需要更新进度信息
                        if (currentTime - lastUpdateTime >= updateInterval || totalRead == fileSize) {
                            printProgress(totalRead, fileSize); // 打印进度信息
                            lastUpdateTime = currentTime; // 更新上次更新时间
                        }
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            System.err.println("计算文件hash失败: " + file);
        }
        return "";
    }

    private static void printProgress(long totalRead, long fileSize) {
        double progress = (double) totalRead / fileSize * 100; // 计算进度百分比
        String progressBar = getProgressBar(progress); // 获取进度条
        String sizeInfo = formatSize(totalRead) + "/" + formatSize(fileSize); // 格式化大小信息
        // 打印进度信息
        System.out.printf("\r%s %.2f%% (%s)", progressBar, progress, sizeInfo);
    }

    private static String getProgressBar(double progress) {
        int barLength = 100; // 进度条长度
        int filledLength = (int) (progress / 100 * barLength); // 已填充的长度
        return "[" + "=".repeat(filledLength) + " ".repeat(barLength - filledLength) + "]";
    }

    private static String formatSize(long size) {
        if (size < 1024) {
            return size + " B"; // 字节
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0); // KB
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024)); // MB
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024)); // GB
        }
    }

}