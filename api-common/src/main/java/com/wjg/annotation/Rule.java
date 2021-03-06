package com.wjg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 入参出参的定义
 * @author wjg
 * @date 2018/8/14 14:41
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
    /**
     * 名称
     * @return
     */
    String name() default "";

    /**
     * 数据类型
     * @return
     */
    String type() default "String";

    /**
     * 字段描述
     * @return
     */
    String desc() default "";

}
