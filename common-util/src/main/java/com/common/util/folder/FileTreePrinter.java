package com.common.util.folder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileTreePrinter {
    static class TreeNode {
        String name;
        Map<String, TreeNode> children;

        TreeNode(String name) {
            this.name = name;
            this.children = new TreeMap<>(); // 使用TreeMap保证子节点顺序
        }
    }

    public static void print(List<String> args) {
        // 构建树
        TreeNode root = buildFileTree(args);
        if (root == null) {
            return;
        }
        // 打印树结构
        printTree(root, "", true);
    }

    private static TreeNode buildFileTree(List<String> args) {
        // 参数校验
        if (args == null || args.isEmpty()) {
            return null;
        }

        // 规范化路径并排序
        List<String> normalizedPaths = args.stream()
                .map(path -> path.replaceAll("\\\\", "/"))  // 统一分隔符
                .sorted()
                .toList();

        // 构建树根节点
        TreeNode root = new TreeNode("");

        // 构建树结构
        for (String path : normalizedPaths) {
            String[] parts = path.split("/");
            TreeNode current = root;

            // 逐层构建树
            for (String part : parts) {
                current.children.putIfAbsent(part, new TreeNode(part));
                current = current.children.get(part);
            }
        }
        return root;
    }

    private static void printTree(TreeNode node, String prefix, boolean isLast) {
        if (node.name.isEmpty()) {
            // 根节点不打印
            List<TreeNode> rootChildren = new ArrayList<>(node.children.values());
            for (int i = 0; i < rootChildren.size(); i++) {
                printTree(rootChildren.get(i), "", i == rootChildren.size() - 1);
            }
            return;
        }

        // 打印当前节点
        if (isLast) {
            System.out.println(prefix + "└── " + node.name);
        } else {
            System.out.println(prefix + "├── " + node.name);
        }

        // 打印子节点
        List<TreeNode> children = new ArrayList<>(node.children.values());
        for (int i = 0; i < children.size(); i++) {
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            printTree(children.get(i), newPrefix, i == children.size() - 1);
        }
    }

}
