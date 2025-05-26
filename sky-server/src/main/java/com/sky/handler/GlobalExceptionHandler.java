package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获sql异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库完整性约束异常：", ex);  // 完整记录异常堆栈
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String duplicateValue = split[2];
            String msg = duplicateValue + MessageConstant.ALREADY_EXISTS;
            // 唯一约束冲突
            return Result.error(msg);
        } else if (message.contains("cannot be null")) {
            // 非空约束
            return Result.error("必填字段不能为空");
        } else if (message.contains("foreign key constraint")) {
            // 外键约束
            return Result.error("关联数据不存在，请检查");
        }

        return Result.error("数据操作失败，请检查数据有效性");
    }

}
