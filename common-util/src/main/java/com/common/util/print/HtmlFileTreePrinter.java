package com.common.util.print;

import java.io.*;
import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class HtmlFileTreePrinter {
    private static final String HTML_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                    }
            
                    html, body {
                        height: 100%%;
                        overflow: hidden;
                    }
        
                    body {
                        background-color: #ededed;
                        color: #000000;
                    }
      
                    .container {
                        height: 100%%;
                        max-width: 800px;
                        margin: 0 auto;
                        background: #fff;
                        display: flex;
                        flex-direction: column;
                    }
            
                    .header {
                        padding: 20px;
                        background: #fff;
                        border-bottom: 1px solid #e6e6e6;
                        flex-shrink: 0;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
            
                    .title {
                        font-size: 18px;
                        font-weight: bold;
                        color: #333;
                    }
            
                    .search-wrapper {
                        width: 300px;
                    }
            
                    .search-box {
                        background: #f5f5f5;
                        border-radius: 4px;
                        padding: 8px 12px;
                        display: flex;
                        align-items: center;
                    }
            
                    .search-box input {
                        border: none;
                        background: transparent;
                        width: 100%%;
                        outline: none;
                        margin-left: 8px;
                        font-size: 14px;
                    }
            
                    .search-icon {
                        color: #888;
                        font-size: 14px;
                    }
            
                    .tree-container {
                        flex: 1;
                        overflow-y: auto;
                        padding: 10px 0;
                    }
            
                    .tree-item {
                        padding: 12px 20px;
                        display: flex;
                        align-items: center;
                        cursor: pointer;
                        transition: background-color 0.2s;
                    }
            
                    .tree-item:hover {
                        background-color: #f5f5f5;
                    }
            
                    .tree-item:active {
                        background-color: #f0f0f0;
                    }
            
                    .tree-item i {
                        margin-right: 10px;
                        color: #07c160;
                    }
            
                    .tree-item.file i {
                        color: #888;
                    }
            
                    .tree-item span {
                        font-size: 14px;
                        color: #333;
                    }
            
                    .tree-content {
                        margin-left: 20px;
                        border-left: 1px solid #f0f0f0;
                    }
            
                    .hidden {
                        display: none;
                    }
            
                    .arrow {
                        display: inline-block;
                        width: 8px;
                        height: 8px;
                        border-right: 2px solid #888;
                        border-bottom: 2px solid #888;
                        transform: rotate(-45deg);
                        margin-right: 10px;
                        transition: transform 0.2s;
                    }
            
                    .arrow.expanded {
                        transform: rotate(45deg);
                    }
            
                    .no-results {
                        padding: 20px;
                        text-align: center;
                        color: #888;
                        font-size: 14px;
                        display: none;
                    }
            
                    .status-bar {
                        padding: 10px 20px;
                        background: #f9f9f9;
                        border-top: 1px solid #e6e6e6;
                        color: #666;
                        font-size: 12px;
                        flex-shrink: 0;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
            
                    .base-path-select {
                        padding: 4px 8px;
                        border: 1px solid #ddd;
                        border-radius: 4px;
                        font-size: 12px;
                        outline: none;
                        background: #fff;
                        max-width: 300px;
                    }
            
                    .base-path-select option {
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="title">%s</div>
                        <div class="search-wrapper">
                            <div class="search-box">
                                <span class="search-icon">🔍</span>
                                <input type="text" id="searchInput" placeholder="搜索">
                            </div>
                        </div>
                    </div>
                    <div class="tree-container" id="fileTree">
                        %s
                    </div>
                    <div class="no-results">
                        未找到相关内容
                    </div>
                    <div class="status-bar">
                        <div>
                            总计: <span id="totalCount">0</span> 项 |
                            当前显示: <span id="visibleCount">0</span> 项
                        </div>
                        <select class="base-path-select" id="basePathSelect">
                            %s
                        </select>
                    </div>
                </div>
            
                <script>
                    document.addEventListener('DOMContentLoaded', function() {
                        // 初始化时隐藏所有子目录（第一层除外）
                        document.querySelectorAll('.tree-content').forEach(content => {
                            const level = getLevel(content);
                            if (level > 1) {
                                content.classList.add('hidden');
                            }
                        });
            
                        // 获取元素的层级
                        function getLevel(element) {
                            let level = 0;
                            let current = element;
                            while (current && !current.id.includes('fileTree')) {
                                if (current.classList.contains('tree-content')) {
                                    level++;
                                }
                                current = current.parentElement;
                            }
                            return level;
                        }
            
                        // 获取节点的完整路径
                        function getNodePath(element) {
                            const parts = [];
                            let current = element;
                            while (current && !current.id.includes('fileTree')) {
                                if (current.classList.contains('tree-item')) {
                                    const nameSpan = current.querySelector('span:not(.arrow)');
                                    if (nameSpan) {
                                        parts.unshift(nameSpan.textContent);
                                    }
                                }
                                current = current.parentElement;
                            }
                            return parts.join('/');
                        }
            
                        // 处理文件点击
                        document.querySelectorAll('.tree-item.file').forEach(item => {
                            item.addEventListener('click', function(e) {
                                e.stopPropagation();
                                const basePath = document.getElementById('basePathSelect').value;
                                const relativePath = this.getAttribute('data-path');
                                if (relativePath) {
                                    const fullPath = basePath + '/' + relativePath;
                                    window.open('file:///' + fullPath.replace(/\\\\/g, '/'), '_blank');
                                }
                            });
                        });
            
                        // 处理文件夹点击
                        document.querySelectorAll('.tree-item:not(.file)').forEach(item => {
                            item.addEventListener('click', function(e) {
                                e.stopPropagation();
                                const content = this.nextElementSibling;
                                if (content && content.classList.contains('tree-content')) {
                                    content.classList.toggle('hidden');
                                    const arrow = this.querySelector('.arrow');
                                    if (arrow) {
                                        arrow.classList.toggle('expanded');
                                    }
                                }
                            });
                        });
            
                        // 更新计数
                        function updateCounts() {
                            const total = document.querySelectorAll('.tree-item').length;
                            const visible = document.querySelectorAll('.tree-item:not([style*="display: none"])').length;
                            document.getElementById('totalCount').textContent = total;
                            document.getElementById('visibleCount').textContent = visible;
                        }
            
                        // 初始计数
                        updateCounts();
            
                        // 搜索功能
                        const searchInput = document.getElementById('searchInput');
                        const noResults = document.querySelector('.no-results');
            
                        searchInput.addEventListener('input', function() {
                            const searchText = this.value.toLowerCase();
                            let hasResults = false;
            
                            document.querySelectorAll('.tree-item').forEach(item => {
                                const itemText = item.textContent.toLowerCase();
                                const shouldShow = itemText.includes(searchText);
            
                                if (shouldShow) {
                                    hasResults = true;
                                    item.style.display = '';
                                    let parent = item.parentElement;
                                    while (parent && parent.classList.contains('tree-content')) {
                                        parent.classList.remove('hidden');
                                        parent = parent.parentElement;
                                    }
                                } else {
                                    item.style.display = 'none';
                                }
                            });
            
                            noResults.style.display = hasResults ? 'none' : 'block';
                            updateCounts();
                        });
                    });
                </script>
            </body>
            </html>
            """;

    public static void print(List<String> paths, List<String> basePaths, String title) {
        TreeNode root = TreeNode.buildFileTree(paths);
        if (root == null) return;

        String basePathOptions = generateBasePathOptions(basePaths);
        String htmlContent = generateHtmlTree(root, basePathOptions, title);
        File outputFile = writeHtmlFile(htmlContent);
        openInBrowser(outputFile);
    }

    private static String generateBasePathOptions(List<String> basePaths) {
        if (basePaths == null || basePaths.isEmpty()) {
            return "";
        }

        return basePaths.stream()
                .map(path -> String.format("<option value=\"%s\">%s</option>",
                        path.replace("\\", "/"),
                        path.replace("\\", "/")))
                .collect(Collectors.joining("\n"));
    }

    private static String generateHtmlTree(TreeNode node, String basePathOptions, String title) {
        StringBuilder sb = new StringBuilder();
        generateHtmlTreeContent(node, sb);
        return String.format(HTML_TEMPLATE, title, title, sb, basePathOptions);
    }

    private static void generateHtmlTreeContent(TreeNode node, StringBuilder sb) {
        if (node.name.isEmpty()) {
            for (TreeNode child : node.children.values()) {
                generateHtmlTreeContent(child, sb);
            }
            return;
        }

        String displayName = node.getCompressedName();
        boolean hasChildren = !node.getActualChildren().isEmpty();

        sb.append("<div class='tree-item")
          .append(hasChildren ? "" : " file")
          .append("' data-path='")
          .append(node.getOriginalPath())
          .append("'>");
        
        if (hasChildren) {
            sb.append("<span class='arrow'></span>");
        }
        sb.append("<i>").append(hasChildren ? "📁" : "📄").append("</i>");
        sb.append("<span>").append(displayName).append("</span>");
        sb.append("</div>");

        if (hasChildren) {
            sb.append("<div class='tree-content'>");
            for (TreeNode child : node.getActualChildren().values()) {
                generateHtmlTreeContent(child, sb);
            }
            sb.append("</div>");
        }
    }

    private static File writeHtmlFile(String content) {
        try {
            File tempFile = File.createTempFile("fileTree", ".html");
            try (Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                writer.write(content);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("生成HTML文件失败", e);
        }
    }

    private static void openInBrowser(File htmlFile) {
        try {
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException e) {
            throw new RuntimeException("打开浏览器失败", e);
        }
    }
}