package com.wjg.annotation;

import java.lang.annotation.*;

/**
 * API的注解
 * @author wjg
 * @date 2018/8/14 14:33
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {
    /**
     * 接口名称
     * @return
     */
    String name() default "";
    /**
     * 作者
     * @return
     */
    String author() default "";

    /**
     * 创建时间
     * @return
     */
    String createTime() default "";

    /**
     * 接口说明
     * @return
     */
    String desc() default "";

    /**
     * 入参数组
     * @return
     */
    Rule [] params() default {};

    /**
     * 返回值数组
     * @return
     */
    Rule [] returns() default {};

    /**
     * 请求示例
     * @return
     */
    Example example() default @Example;

    /**
     * 备注
     * @return
     */
    String remark() default "";

}
