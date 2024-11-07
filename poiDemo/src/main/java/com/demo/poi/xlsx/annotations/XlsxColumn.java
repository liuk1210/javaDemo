package com.demo.poi.xlsx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 作者：Liuk
 * 创建日期：2023-11-21
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsxColumn {
    String value() default "";

    int width() default 3000;

    String annotations() default "";

    String[] combobox() default {};

}
