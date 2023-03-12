package com.ysx.common.security;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.ysx.mapper.SysRoleMapper;
import com.ysx.pojo.SysRole;
import com.ysx.pojo.SysUser;
import com.ysx.service.SysMenuService;
import com.ysx.service.SysRoleService;
import com.ysx.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/06/20:34
 * @Description:
 */
@Service
public class MyUserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return null;
        SysUser user = userService.getUserByName(username);
        if(user==null){
            throw new RuntimeException("用户不存在，请注册");
        }
        if("1".equals(user.getStatus())){
            throw new RuntimeException("用户已注销");
        }
        return new User(user.getUsername(), user.getPassword(), getauthorities(user.getId()));
    }

    public List<? extends GrantedAuthority> getauthorities(Long userId) {
        String userAuthortyInfo = userService.getUserAuthortyInfo(userId);
//        System.out.println(userAuthortyInfo);
        return AuthorityUtils.commaSeparatedStringToAuthorityList(userAuthortyInfo);
    }
}
