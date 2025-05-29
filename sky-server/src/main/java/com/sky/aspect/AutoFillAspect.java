package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点定义
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 前置通知，在目标方法执行前执行自动填充
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        try {
            // 获取方法签名和注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AutoFill autoFill = method.getAnnotation(AutoFill.class);
            OperationType operationType = autoFill.value();

            // 获取方法参数
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return;
            }

            // 获取实体对象（假设第一个参数是要操作的实体）
            Object entity = args[0];
            LocalDateTime now = LocalDateTime.now();
            Long currentId = BaseContext.getCurrentId();

            // 根据操作类型设置不同字段
            if (operationType == OperationType.INSERT) {
                // 插入操作
                setFieldValue(entity, "createTime", now);
                setFieldValue(entity, "updateTime", now);
                setFieldValue(entity, "createUser", currentId);
                setFieldValue(entity, "updateUser", currentId);
            } else if (operationType == OperationType.UPDATE) {
                // 更新操作
                setFieldValue(entity, "updateTime", now);
                setFieldValue(entity, "updateUser", currentId);
            }

        } catch (Exception e) {
            log.error("公共字段自动填充失败", e);
            throw new RuntimeException("公共字段自动填充失败", e);
        }
    }

    /**
     * 通过反射设置字段值
     * @param target 目标对象
     * @param fieldName 字段名
     * @param value 要设置的值
     */
    private void setFieldValue(Object target, String fieldName, Object value) {
        try {
            // 获取字段
            Field field = target.getClass().getDeclaredField(fieldName);
            // 设置可访问
            field.setAccessible(true);
            // 设置字段值
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            // 字段不存在时忽略（不是所有实体都有全部字段）
            log.debug("实体 {} 不存在字段 {}", target.getClass().getSimpleName(), fieldName);
        } catch (IllegalAccessException e) {
            log.error("设置字段 {} 值失败", fieldName, e);
            throw new RuntimeException("设置字段值失败", e);
        }
    }
}