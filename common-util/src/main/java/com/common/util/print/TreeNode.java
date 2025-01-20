package com.common.util.print;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TreeNode {
    String name;
    Map<String, TreeNode> children;
    // 新增：保存原始路径
    String originalPath;

    TreeNode(String name) {
        this.name = name;
        this.children = new TreeMap<>();
        this.originalPath = "";
    }

    // 新增：获取原始路径的方法
    String getOriginalPath() {
        return originalPath;
    }

    // 修改：获取压缩后的路径名时，同时更新 originalPath
    String getCompressedName() {
        StringBuilder path = new StringBuilder(name);
        TreeNode current = this;
        String fullPath = originalPath;
        
        // 当节点只有一个子节点时，进行路径压缩
        while (current.children.size() == 1) {
            TreeNode child = current.children.values().iterator().next();
            path.append("/").append(child.name);
            // 更新当前节点的原始路径为叶子节点的路径
            if (child.originalPath != null && !child.originalPath.isEmpty()) {
                fullPath = child.originalPath;
            }
            current = child;
        }
        
        // 更新压缩节点的原始路径
        this.originalPath = fullPath;
        return path.toString();
    }

    // 修改：获取实际的子节点时，同时处理原始路径
    Map<String, TreeNode> getActualChildren() {
        TreeNode current = this;
        while (current.children.size() == 1) {
            TreeNode child = current.children.values().iterator().next();
            // 传递原始路径给父节点
            if (child.originalPath != null && !child.originalPath.isEmpty()) {
                this.originalPath = child.originalPath;
            }
            current = child;
        }
        return current.children;
    }

    public static TreeNode buildFileTree(List<String> args) {
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
            // 在叶子节点保存原始路径
            current.originalPath = path;
        }
        return root;
    }
}
