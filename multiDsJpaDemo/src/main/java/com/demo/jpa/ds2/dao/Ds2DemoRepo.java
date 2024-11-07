package com.demo.jpa.ds2.dao;

import com.alibaba.fastjson2.JSONObject;
import com.demo.jpa.ds2.entity.Ds2DemoEntity;
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
public interface Ds2DemoRepo extends JpaRepository<Ds2DemoEntity,Long> {

    @Query(value = """
        select 'ds2' as prop1 from dual
    """,nativeQuery = true)
    List<JSONObject> list();
}
