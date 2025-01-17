package com.common.util.folder;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.common.util.sha256.Sha256Util.calculateSHA256;

@Slf4j
public class FolderComparator {

    public static boolean PRINT_FOLDER_MISS = true;       //是否打印缺失文件夹
    public static boolean PRINT_FILE_MISS = true;         //是否打印缺失文件
    public static boolean PRINT_FILE_DIFF = true;         //是否打印差异文件

    public static void main(String[] args) throws IOException {
        compareFolders("C:\\Workspaces\\Projects", "V:\\Projects");
    }

    public static void compareFolders(String path1, String path2) throws IOException {
        Path basePath1 = Paths.get(path1);
        // 检查路径是否存在
        if (!Files.exists(basePath1)) {
            System.out.printf("%s不存在", path1);
            return;
        }

        Path basePath2 = Paths.get(path2);
        // 检查路径是否存在
        if (!Files.exists(basePath2)) {
            System.out.printf("%s不存在", path2);
            return;
        }

        // 收集同路径文件
        List<Path> samePathFile = collectFileDifferences(basePath1, basePath2);

        // 多线程处理SHA256比较同路径文件
        compareFilesInParallel(samePathFile, basePath1, basePath2);

    }

    private static List<Path> collectFileDifferences(Path basePath1, Path basePath2) throws IOException {
        List<String> inPath1NotInPath2Folder = new ArrayList<>();
        List<String> inPath2NotInPath1Folder = new ArrayList<>();
        List<String> inPath1NotInPath2File = new ArrayList<>();
        List<String> inPath2NotInPath1File = new ArrayList<>();

        List<Path> samePathFile = new ArrayList<>();
        System.out.printf("正在遍历%s中...%n", basePath1);
        Files.walkFileTree(basePath1, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path relativePath = basePath1.relativize(dir);
                Path targetPath = basePath2.resolve(relativePath);

                if (!Files.exists(targetPath)) {
                    inPath1NotInPath2Folder.add(relativePath.toString());
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path relativePath = basePath1.relativize(file);
                Path targetPath = basePath2.resolve(relativePath);
                if (!Files.exists(targetPath)) {
                    inPath1NotInPath2File.add(relativePath.toString());
                } else {
                    samePathFile.add(relativePath);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        printDiffInfo(basePath1, basePath2, inPath1NotInPath2Folder, inPath1NotInPath2File);

        System.out.printf("正在遍历%s中...%n", basePath2);
        // 遍历第二个文件夹
        Files.walkFileTree(basePath2, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path relativePath = basePath2.relativize(dir);
                Path targetPath = basePath1.resolve(relativePath);

                if (!Files.exists(targetPath)) {
                    inPath2NotInPath1Folder.add(relativePath.toString());
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path relativePath = basePath2.relativize(file);
                if (!Files.exists(basePath1.resolve(relativePath))) {
                    inPath2NotInPath1File.add(relativePath.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        printDiffInfo(basePath2, basePath1, inPath2NotInPath1Folder, inPath2NotInPath1File);

        return samePathFile;
    }

    private static void printDiffInfo(Path basePath1, Path basePath2, List<String> inPath1NotInPath2Folder, List<String> inPath1NotInPath2File) {
        System.out.printf("%s目录结构读取完毕.%n", basePath1);
        if (!inPath1NotInPath2Folder.isEmpty() && PRINT_FOLDER_MISS) {
            System.out.printf("%s中不存在以下%d个文件夹：%n", basePath2, inPath1NotInPath2Folder.size());
            inPath1NotInPath2Folder.forEach(System.out::println);
        }
        if (!inPath1NotInPath2File.isEmpty() & PRINT_FILE_MISS) {
            System.out.printf("%s中不存在以下%d个文件：%n", basePath2, inPath1NotInPath2File.size());
            inPath1NotInPath2File.forEach(System.out::println);
        }
        System.out.println();
    }

    private static void compareFilesInParallel(List<Path> commonFiles, Path basePath1, Path basePath2) {
        if (!PRINT_FILE_DIFF) {
            return;
        }
        final int threadCount = Runtime.getRuntime().availableProcessors();
        final int batchSize = Math.max(1, commonFiles.size() / (threadCount * 4)); // 每批次处理的文件数
        AtomicInteger processedFiles = new AtomicInteger(0); // 进度计数器
        final int totalFiles = commonFiles.size();

        CompletableFuture<?>[] futures = Lists.partition(commonFiles, batchSize).stream()
                .map(subList -> CompletableFuture.supplyAsync(() ->
                        processBatch(subList, basePath1, basePath2, processedFiles, totalFiles)
                ))
                .toArray(CompletableFuture[]::new);

        //汇总所有任务执行结果
        List<String> results = CompletableFuture.allOf(futures).thenApply(a -> {
            List<String> rs = new ArrayList<>();
            for (CompletableFuture<?> future : futures) {
                Object futureJoin = future.join();
                if (futureJoin instanceof List<?> list) {
                    if (!list.isEmpty()) {
                        for (Object o : list) {
                            if (o instanceof String str) {
                                rs.add(str);
                            }
                        }
                    }
                }
            }
            return rs;
        }).join();
        results.forEach(System.out::println);
        System.out.printf("%n-----------------比对文件sha256结束，存在以上%d个文件sha256不一致-------------------%n",results.size());
    }

    private static List<String> processBatch(List<Path> subList, Path basePath1, Path basePath2,
                                     AtomicInteger processedFiles, int total) {
        List<String> notEqualFiles = Lists.newArrayList();
        subList.forEach(relativePath -> {
            String hash1 = calculateSHA256(basePath1.resolve(relativePath));
            String hash2 = calculateSHA256(basePath2.resolve(relativePath));
            if (!hash1.equals(hash2) && PRINT_FILE_DIFF) {
                notEqualFiles.add(relativePath.toString());
            }
            processedFiles.incrementAndGet();
            System.out.printf("正在计算并比对sha256中，进度: %d/%d (%.2f%%)\r", processedFiles.get(), total,
                    (processedFiles.get() * 100.0 / total));
        });
        return notEqualFiles;
    }

}
