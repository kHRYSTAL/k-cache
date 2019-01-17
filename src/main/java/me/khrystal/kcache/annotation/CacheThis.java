package me.khrystal.kcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kHRYSTAL on 19/1/17.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheThis {
    Class<? extends Object> model() default DefaultModel.class;

    String key() default "";
}
