package com.demo.mybatis.controller;

import com.demo.mybatis.service.OracleDocsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Oracle数据字典生成")
@RestController
@RequestMapping("oracle")
@RequiredArgsConstructor
public class DocsController {

    private final OracleDocsService oracleDocsService;

    @GetMapping("md")
    @Operation(summary = "根据表名生成数据字典")
    public String docs(@RequestParam("tableName") String tableName) {
        return oracleDocsService.getDocs(tableName);
    }
    
}
