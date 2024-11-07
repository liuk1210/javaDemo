package com.demo.jpa.ds1.dao;

import com.alibaba.fastjson2.JSONObject;
import com.demo.jpa.ds1.entity.Ds1DemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 作者：Liuk
 * 创建日期：2023-12-04
 */
@Repository
public interface Ds1DemoRepo extends JpaRepository<Ds1DemoEntity,Long> {
    @Query(value = """
        select 'ds1' as prop1 from dual
    """,nativeQuery = true)
    List<JSONObject> list();
}
