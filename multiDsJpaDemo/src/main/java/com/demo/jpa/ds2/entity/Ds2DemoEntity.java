package com.demo.jpa.ds2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * <p>
 * 作者：Liuk
 * 创建日期：2023-12-04
 */
@Entity
@Data
public class Ds2DemoEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String prop1;

}
