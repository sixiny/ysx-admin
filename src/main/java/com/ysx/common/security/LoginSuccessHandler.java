package com.ysx.common.security;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysx.pojo.R;
import com.ysx.pojo.SysMenu;
import com.ysx.pojo.SysRole;
import com.ysx.pojo.SysUser;
import com.ysx.service.SysMenuService;
import com.ysx.service.SysRoleService;
import com.ysx.service.SysUserService;
import com.ysx.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/06/19:55
 * @Description: 登录成功处理器
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private SysMenuService menuService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleService roleService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 登录成功返回列表和权限
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();
        String username=authentication.getName();
        String token = JwtUtils.genJwtToken(username);
        //登录成功 查出 menu等返还给前端
        SysUser user = userService.getUserByName(username);

        //查出用户角色
        List<SysRole> roles = roleService.list(new QueryWrapper<SysRole>().inSql("id", "select role_id from sys_user_role where user_id=" + user.getId()));

        //添加用户角色
        user.setRoles(roles.stream().map(e->e.getName()).collect(Collectors.joining(",")));

        Set<SysMenu> menus = new HashSet<>();

        for (SysRole role : roles) {
            List<SysMenu> menulist = menuService.list(new QueryWrapper<SysMenu>().inSql("id", "select menu_id from sys_role_menu where role_id=" + role.getId()));
            menus.addAll(menulist);
        }

        // 把权限传过去让前端判断 有则显示 没有隐藏 (有点小问题 每次重新分配权限后 需要重新登录才会有loginsuccess的处理刷新 可以用filter来一下)
        List<String> perms = menus.stream().map(r -> r.getPerms()).collect(Collectors.toList());

        List<SysMenu> menuList = new ArrayList<>(menus);
        menuList.sort(Comparator.comparing(SysMenu::getOrderNum));  // 排序

        List<SysMenu> sysMenus = menuService.buildTreeMenu(menuList);

//        System.out.println(token);

        //登录成功把东西传给前端
        outputStream.write(JSONUtil.toJsonStr(R.ok("登录成功").put("authorization",token).put("perms",perms).put("menuList",sysMenus).put("currentUser",user)).getBytes());
        outputStream.flush();
        outputStream.close();

    }
}
