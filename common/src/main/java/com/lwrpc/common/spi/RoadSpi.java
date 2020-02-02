package com.lwrpc.common.spi;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoadSpi {
    String value() default "";
}

