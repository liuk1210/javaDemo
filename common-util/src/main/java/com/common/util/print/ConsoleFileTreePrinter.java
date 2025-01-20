package com.common.util.print;

import java.util.ArrayList;
import java.util.List;

public class ConsoleFileTreePrinter {

    public static void print(List<String> args) {
        // 构建树
        TreeNode root = TreeNode.buildFileTree(args);
        if (root == null) {
            return;
        }
        // 打印树结构
        printTree(root, "", true);
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

        // 打印当前节点（使用压缩后的路径名）
        String displayName = node.getCompressedName();
        if (isLast) {
            System.out.println(prefix + "└── " + displayName);
        } else {
            System.out.println(prefix + "├── " + displayName);
        }

        // 打印实际子节点（跳过中间节点）
        List<TreeNode> children = new ArrayList<>(node.getActualChildren().values());
        for (int i = 0; i < children.size(); i++) {
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            printTree(children.get(i), newPrefix, i == children.size() - 1);
        }
    }

}
