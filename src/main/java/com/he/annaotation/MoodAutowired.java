package com.he.annaotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented //javadoc
public @interface MoodAutowired {
    String value() default "";
}
