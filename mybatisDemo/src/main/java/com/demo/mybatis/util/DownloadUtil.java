package com.demo.mybatis.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DownloadUtil {
    /**
     * 根据路径取文件
     *
     * @param request request
     * @param response response
     * @param b byte数组
     * @return 文件
     * @throws IOException IOException
     */
    public static ResponseEntity<byte[]> download(HttpServletRequest request, HttpServletResponse response, byte[] b, String fileName) throws IOException {
        String header = request.getHeader("User-Agent").toUpperCase();
        if (header.contains("MSIE") || header.contains("TRIDENT") || header.contains("EDGE")) {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            fileName = fileName.replace("+", "%20"); // IE下载文件名空格变+号问题
        } else {
            fileName = new String(fileName.getBytes(), "ISO8859-1");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        response.setCharacterEncoding("UTF-8");
        return new ResponseEntity<>(b, headers, HttpStatus.OK);
    }
}
