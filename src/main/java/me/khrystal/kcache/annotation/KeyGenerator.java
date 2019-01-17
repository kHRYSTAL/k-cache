package me.khrystal.kcache.annotation;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kHRYSTAL on 19/1/17.
 */
public class KeyGenerator {

    public static String getCacheBreakKey(ProceedingJoinPoint thisJoinPoint) {
        try {
            String methodName = thisJoinPoint.getSignature().getName();
            Class<?> classTarget = thisJoinPoint.getTarget().getClass();
            Class<?>[] params = ((MethodSignature) thisJoinPoint.getSignature()).getParameterTypes();
            Method objMethod = classTarget.getMethod(methodName, params);
            CacheBreak annotationCache = objMethod.getAnnotation(CacheBreak.class);
            if (annotationCache != null) {
                String modelName = annotationCache.model().getName();
                String key = annotationCache.key();
                if (modelName.equals("me.khrystal.kcache.annotation.DefaultModel")) {
                    modelName = thisJoinPoint.getTarget().getClass().getName();
                }
                key = checkCacheKey(key, thisJoinPoint);
                return modelName + "." + key;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCacheThisKey(ProceedingJoinPoint thisJoinPoint) {
        try {
            String methodName = thisJoinPoint.getSignature().getName();
            Class<?> classTarget = thisJoinPoint.getTarget().getClass();
            Class<?>[] params = ((MethodSignature) thisJoinPoint.getSignature()).getParameterTypes();
            Method objMethod = classTarget.getMethod(methodName, params);
            CacheThis annotationCache = objMethod.getAnnotation(CacheThis.class);
            if (annotationCache != null) {
                String modelName = annotationCache.model().getName();
                String key = annotationCache.key();
                if (modelName.equals("me.khrystal.kcache.annotation.DefaultModel")) {
                    modelName = thisJoinPoint.getTarget().getClass().getName();
                }
                key = checkCacheKey(key, thisJoinPoint);
                return modelName + "." + key;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String checkCacheKey(String key, ProceedingJoinPoint thisJoinPoint) throws Exception {
        String tempKey = key;
        if (tempKey.equals("")) {
            StringBuffer keyStrBuffer = new StringBuffer();
            String method = ((MethodSignature) thisJoinPoint.getSignature()).getMethod().getName();
            keyStrBuffer.append(method + "(");
            for (final Object arg : thisJoinPoint.getArgs()) {
                keyStrBuffer.append(arg.getClass().getSimpleName() + "=" + arg + ";");
            }
            keyStrBuffer.append(")");
            tempKey = keyStrBuffer.toString();
        } else {
            tempKey = getDynamicVal(thisJoinPoint, tempKey);
        }
        return key;
    }

    private static String getDynamicVal(ProceedingJoinPoint joinPoint, String key) throws Exception {
        if (key.indexOf("#") >= 0) {
            Class<?> clazz = joinPoint.getTarget().getClass();
            String clazzName = clazz.getName();
            String methodName = joinPoint.getSignature().getName(); //获取方法名称
            Object[] args = joinPoint.getArgs();//参数
            //获取参数名称和值
            Map<String, Object> namesAndArgs = getArgsMap(clazz, clazzName, methodName, args);
            // 去除所有的加号『+』
            key = key.replace("+", "");
            // 首先根据空格拆分
            StringBuffer stringBuffer = new StringBuffer();
            String[] arrKey = key.split(" ");
            for (String aKey : arrKey) {
                if (!aKey.trim().equals("")) {
                    if (!aKey.contains("#")) {
                        stringBuffer.append(aKey.trim());
                    } else { // 然后按照『#』拆分
                        int firstSharpIndex = aKey.indexOf("#");
                        if (firstSharpIndex > 0) {
                            stringBuffer.append(aKey.substring(0, firstSharpIndex));
                            aKey = aKey.substring(firstSharpIndex);
                        }
                        String[] arrParamNames = aKey.split("#");
                        for (String paramName : arrParamNames) {
                            if (paramName.contains(".")) {
                                Object obj = namesAndArgs.get(paramName.substring(0, paramName.indexOf(".")));
                                if (obj != null) {
                                    Object o = getFieldValueByName(paramName.substring(paramName.indexOf(".") + 1), obj);
                                    stringBuffer.append(o == null ? "" : o.toString());
                                }

                            } else {
                                Object obj = namesAndArgs.get(paramName);
                                stringBuffer.append(obj == null ? "" : obj.toString());
                            }
                        }
                    }
                }
            }
            return stringBuffer.toString();
        } else {
            return key;
        }
    }

    /**
     * 获取切入方法的参数及参数值
     */
    private static Map<String, Object> getArgsMap(Class cls, String clazzName, String methodName, Object[] args) throws NotFoundException {
        Map<String, Object> map = new HashMap<>();
        ClassPool pool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(cls);
        pool.insertClassPath(classPath);
        CtClass cc = pool.get(clazzName);
        CtMethod cm = cc.getDeclaredMethod(methodName);
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < cm.getParameterTypes().length; i++) {
            map.put(attr.variableName(i + pos), args[i]);
        }
        return map;
    }

    private static Object getFieldValueByName(String fieldName, Object o) {
        if (!fieldName.contains(".")) {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method;
            Object value;
            try {
                method = o.getClass().getMethod(getter, new Class[]{});
                value = method.invoke(o, new Object[]{});
            } catch (Exception e) {
                value = getFieldByClazz(fieldName, o);
            }
            return value;
        } else {
            fieldName = fieldName.substring(0, fieldName.indexOf("."));
            String subFieldName = fieldName.substring(fieldName.indexOf(".") + 1);
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method;
            Object value;
            try {
                method = o.getClass().getMethod(getter, new Class[]{});
                value = method.invoke(o, new Object[]{});
            } catch (Exception e) {
                value = getFieldByClazz(fieldName, o);
            }
            if (value != null) {
                return getFieldValueByName(subFieldName, value);
            } else {
                return null;
            }
        }


    }

    private static Object getFieldByClazz(String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
