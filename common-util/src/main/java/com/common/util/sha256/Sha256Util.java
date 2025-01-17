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
            long startTime = System.currentTimeMillis(); // 开始时间
            long lastUpdateTime = startTime; // 上次更新时间
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
                            long totalReadTime = currentTime - startTime; // 读取时间
                            printProgress(totalRead, fileSize, totalReadTime); // 打印进度信息
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

    private static void printProgress(long totalRead, long fileSize, long totalReadTime) {
        double progress = (double) totalRead / fileSize * 100; // 计算进度百分比
        String progressBar = getProgressBar(progress); // 获取进度条
        String sizeInfo = formatSize(totalRead) + "/" + formatSize(fileSize); // 格式化大小信息
        String timeInfo = formatTime(totalReadTime); // 格式化总读取耗时
        double averageSpeed = (double) totalRead / totalReadTime * 1000; // 计算平均读取速度（字节/秒）
        String speedInfo = formatSpeed(averageSpeed); // 格式化平均读取速度
        // 打印进度信息
        System.out.printf("\r%s %.2f%% (%s) [速度：%s，耗时：%s]", progressBar, progress, sizeInfo, speedInfo, timeInfo);
    }

    private static String getProgressBar(double progress) {
        int barLength = 50; // 进度条长度
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

    /**
     * 格式化时间（按 ms 或 s 显示）
     */
    private static String formatTime(long timeMs) {
        if (timeMs < 1000) {
            return String.format("%d ms", timeMs); // 毫秒
        } else {
            return String.format("%.2f s", timeMs / 1000.0); // 秒
        }
    }

    /**
     * 格式化速度（按 KB/s、MB/s 或 GB/s 显示）
     */
    private static String formatSpeed(double speedBytesPerSecond) {
        if (speedBytesPerSecond < 1024) {
            return String.format("%.2f B/s", speedBytesPerSecond); // 字节/秒
        } else if (speedBytesPerSecond < 1024 * 1024) {
            return String.format("%.2f KB/s", speedBytesPerSecond / 1024); // KB/s
        } else if (speedBytesPerSecond < 1024 * 1024 * 1024) {
            return String.format("%.2f MB/s", speedBytesPerSecond / (1024 * 1024)); // MB/s
        } else {
            return String.format("%.2f GB/s", speedBytesPerSecond / (1024 * 1024 * 1024)); // GB/s
        }
    }

}
