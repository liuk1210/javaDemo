package com.demo.mybatis.controller;

import com.demo.mybatis.entity.HttpResult;
import com.demo.mybatis.service.TableService;
import com.demo.mybatis.util.DownloadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "代码生成工具-Mybatis")
@RestController
@RequestMapping("mybatis")
public class MybatisController {
    @Resource
    private TableService tableService;

    @GetMapping("table/generateCode")
    @Operation(summary = "根据表名生成代码", description = "生成mybatis相关代码")
    @Parameters({
            @Parameter(name = "tableName", description = "表名", example = "w_table_name"),
            @Parameter(name = "packageName", description = "包名，生成的代码文件包名",example = "com.a.table"),
            @Parameter(name = "ignorePrefix", description = "忽略表名前缀，为空则默认按照表名驼峰命名", example = "w_")
    })
    public ResponseEntity<byte[]> tableInfo(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam String tableName,
                                            @RequestParam String packageName,
                                            @RequestParam(value = "ignorePrefix" ,required = false) String ignorePrefix
    ) throws Exception {
        byte[] b = tableService.getTableInfo(tableName,packageName,ignorePrefix);
        return DownloadUtil.download(request, response, b, packageName + ".zip");
    }

    @GetMapping("all/table")
    @Operation(summary = "获取所有表")
    public HttpResult<List<String>> listAllTableName() {
        List<String> list = tableService.listAllTableName();
        return new HttpResult<>(list);
    }

}
