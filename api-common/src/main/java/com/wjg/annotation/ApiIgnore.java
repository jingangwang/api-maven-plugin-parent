package com.wjg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定不需要生成API文档的类或者方法
 * @author wjg
 * @date 2018/8/17 14:08
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ApiIgnore{

    String value() default "";
}
