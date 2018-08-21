package com.wjg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 状态码定义
 * @author wjg
 * @date 2018/8/14 14:41
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiResponse {
    /**
     * 返回码
     * @return
     */
   String code() default "";

    /**
     * 返回状态码的描述
     * @return
     */
   String msg() default "";
}
