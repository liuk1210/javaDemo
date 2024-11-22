package com.demo.mybatis.controller;

import com.demo.mybatis.service.MysqlTableService;
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

@Tag(name = "代码生成工具-MybatisPlus")
@RestController
@RequestMapping("mybatisPlus")
public class MysqlMybatisPlusController {
    @Resource
    private MysqlTableService mysqlTableService;

    @GetMapping("table/generateCode")
    @Operation(summary = "根据表名生成代码", description = "生成mybatis相关代码（Swagger-v3注解）")
    @Parameters({
            @Parameter(name = "tableName", description = "表名", example = "w_table_name"),
            @Parameter(name = "packageName", description = "包名，生成的代码文件包名",example = "com.a.table"),
            @Parameter(name = "moduleName", description = "模块名称")
    })
    public ResponseEntity<byte[]> tableInfo(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam String tableName,
                                            @RequestParam String packageName,
                                            @RequestParam(value = "moduleName" ,required = false) String moduleName
    ) throws Exception {
        byte[] b = mysqlTableService.getTableInfo(tableName,packageName,moduleName);
        return DownloadUtil.download(request, response, b, packageName + ".zip");
    }

}
