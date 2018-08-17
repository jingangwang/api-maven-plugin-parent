package com.wjg.annotation;

import java.lang.annotation.*;

/**
 * @author wjg
 * @date 2018/8/15 10:06
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Model {
    /**
     * 所属模块的名字
     * @return
     */
    String value() default "";

    /**
     * 所属模块的描述
     * @return
     */
    String desc() default "";
}
