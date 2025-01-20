package com.common.util.print;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TreeNode {
    String name;
    Map<String, TreeNode> children;

    TreeNode(String name) {
        this.name = name;
        this.children = new TreeMap<>();
    }

    // 新增：获取压缩后的路径名
    String getCompressedName() {
        StringBuilder path = new StringBuilder(name);
        TreeNode current = this;
        // 当节点只有一个子节点时，进行路径压缩
        while (current.children.size() == 1) {
            TreeNode child = current.children.values().iterator().next();
            path.append("/").append(child.name);
            current = child;
        }
        return path.toString();
    }

    // 新增：获取实际的子节点（跳过中间只有单个子节点的情况）
    Map<String, TreeNode> getActualChildren() {
        TreeNode current = this;
        while (current.children.size() == 1) {
            current = current.children.values().iterator().next();
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
        }
        return root;
    }
}
