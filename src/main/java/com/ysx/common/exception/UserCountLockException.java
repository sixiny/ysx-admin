package com.ysx.common.exception;

import javax.naming.AuthenticationException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/06/20:50
 * @Description:
 */
public class UserCountLockException extends AuthenticationException {
    public UserCountLockException(String explanation) {
        super(explanation);
    }

    public UserCountLockException() {
        super();
    }
}
