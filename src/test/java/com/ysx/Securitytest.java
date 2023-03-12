package com.ysx;

import com.ysx.pojo.SysUser;
import com.ysx.service.SysMenuService;
import com.ysx.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/06/20:59
 * @Description:
 */
@SpringBootTest
public class Securitytest {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SysUserService service;


    @Autowired
    private SysMenuService menuService;

    @Test
    public void test(){
        SysUser user = new SysUser();
        user.setUsername("ysx");
        user.setPassword(passwordEncoder.encode(""));
        service.save(user);

    }

    @Test
    public void test1(){
        SysUser ysx = service.getUserByName("ysx");
        System.out.println(ysx);

    }
}
