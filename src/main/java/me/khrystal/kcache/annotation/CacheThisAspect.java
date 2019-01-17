package me.khrystal.kcache.annotation;

import me.khrystal.kcache.concurrent.DataCoordinate;
import me.khrystal.kcache.console.Console;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by kHRYSTAL on 19/1/17.
 */
@Component
@Aspect
@Order(4)
public class CacheThisAspect {
    @Autowired
    private DataCoordinate coordinate;

    @Pointcut(value = "execution(@me.khrystal.kcache.annotation.CacheThis * * (..))")
    public void cache() {
    }

    public Object aroundCacheMethods(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        long start;
        long end;
        start = System.currentTimeMillis();
        String key = KeyGenerator.getCacheThisKey(thisJoinPoint);
        if (key == null || key.trim().equals("")) {
            return thisJoinPoint.proceed();
        }
        Object value = coordinate.getLevel1Val(key);
        if (value == null) {
            if (coordinate.isLevel2Usable()) {
                value = coordinate.getLevel2Val(key);
                if (value == null) {
                    value = thisJoinPoint.proceed();
                    if (value == null) {
                        coordinate.fulFillLevel1NullValue(key);
                    } else {
                        coordinate.fulFillLevel1(key, value);
                        coordinate.fulFillLevel2(key, value);
                    }
                } else {
                    end = System.currentTimeMillis();
                    Console.println("[K-Cache] 二级缓存命中, key:" + key + ", cost time: " + (end - start) + "ms.");
                }
            } else {
                value = thisJoinPoint.proceed();
                if (null == value) {
                    coordinate.fulFillLevel1NullValue(key);
                } else {
                    coordinate.fulFillLevel1(key, value);
                }
            }
        } else {
            end = System.currentTimeMillis();
            Console.println("[K-Cache] 一级缓存命中, key:" + key + ", cost time: " + (end - start) + "ms.");
            if (value instanceof NullValObject) {
                return null;
            }
        }
        return value;
    }
}
