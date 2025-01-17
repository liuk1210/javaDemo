package com.common.util.sha256;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

public class Sha256Util {

    public static String calculateSHA256(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 使用直接缓冲区
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024); // 1MB

            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                while (channel.read(buffer) > 0) {
                    buffer.flip(); // 切换为读模式
                    digest.update(buffer); // 直接更新 MessageDigest
                    buffer.clear(); // 清空缓冲区
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            System.err.println("计算文件hash失败: " + file);
        }
        return "";
    }

}