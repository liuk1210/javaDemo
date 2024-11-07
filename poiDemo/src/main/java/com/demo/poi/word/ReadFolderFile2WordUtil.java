package com.demo.poi.word;



import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 递归读取文件夹所有文件内容，并将内容存储到word文档中
 * <p>
 * 作者：Liuk
 * 创建日期：2023-11-28
 */
@Slf4j
public class ReadFolderFile2WordUtil {
    public static void main(String[] args) throws IOException {
        XWPFDocument document = new XWPFDocument();
        readFilesRecursively("需要读取的文件夹绝对路径",document);
        FileOutputStream out = new FileOutputStream("D:/out.docx");
        document.write(out);
        out.close();
        document.close();
    }

    private static void createChapterH1(XWPFDocument doc, String content) {
        XWPFParagraph actTheme = doc.createParagraph();
        actTheme.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun runText1 = actTheme.createRun();
        runText1.setBold(true);
        runText1.setText(content);
        runText1.setFontSize(18);
    }
    private static void createParagraph(XWPFDocument doc, String content) {
        XWPFParagraph actType = doc.createParagraph();
        XWPFRun runText2 = actType.createRun();
        runText2.setText(content);
        runText2.setFontSize(11);
    }
    public static void readFilesRecursively(String folderPath,XWPFDocument document) {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    readFilesRecursively(file.getAbsolutePath(),document);
                }
            }
        } else {
            try {
                createChapterH1(document,folder.getName());
                List<String> lines = Files.readAllLines(Paths.get(folder.getAbsolutePath()));
                for (String line : lines) {
                    createParagraph(document,line);
                }
                System.out.println(folder.getName()+"已读取");
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
    }

}
