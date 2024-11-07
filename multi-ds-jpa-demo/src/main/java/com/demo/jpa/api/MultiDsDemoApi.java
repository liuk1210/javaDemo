package com.demo.jpa.api;

import com.alibaba.fastjson2.JSONObject;
import com.demo.jpa.ds1.dao.Ds1DemoRepo;
import com.demo.jpa.ds2.dao.Ds2DemoRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 作者：Liuk
 * 创建日期：2023-12-04
 */
@RestController
@Slf4j
public class MultiDsDemoApi {
    @Resource
    private Ds1DemoRepo ds1DemoRepo;
    @Resource
    private Ds2DemoRepo ds2DemoRepo;

    @GetMapping("test")
    public String test(){
        List<JSONObject> list1 = ds1DemoRepo.list();
        List<JSONObject> list2 = ds2DemoRepo.list();
        log.info(list1.toString());
        log.info(list2.toString());
        return "success";
    }

}
