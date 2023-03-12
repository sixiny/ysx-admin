package com.ysx.common.exception;

import com.ysx.pojo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/06/20:52
 * @Description:全局异常处理
 */
@Slf4j
@RestControllerAdvice  //全局管理注解
public class GlobalException {

    @ExceptionHandler(value = RuntimeException.class)
    public R handle(RuntimeException e){
        log.error("运行时错误" + e.getMessage());
        return R.error(e.getMessage());
    }

}
