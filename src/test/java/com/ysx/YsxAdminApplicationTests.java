package com.ysx;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ysx.common.security.MyUserDetailServiceImpl;
import com.ysx.pojo.SysMenu;
import com.ysx.pojo.SysRole;
import com.ysx.pojo.SysUser;
import com.ysx.service.SysMenuService;
import com.ysx.service.SysRoleService;
import com.ysx.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class YsxAdminApplicationTests {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private SysMenuService menuService;

    @Test
    void contextLoads() {
        System.out.println(userService.getUserAuthortyInfo(1L));

        List<SysRole> list = roleService.list();

        String collect = list.stream().map(e -> e.getName()).collect(Collectors.joining(","));
        System.out.println(collect);
    }

    @Test
    void testUpdate(){
        SysUser user = new SysUser();
        user.setUsername("test1");
        user.setEmail("test1@qq.com");
        user.setId(32L);
        userService.update(user, new UpdateWrapper<SysUser>().eq("id", user.getId()));
    }

    @Test
    void testRole(){
        List<SysRole> user_id = roleService.list(new QueryWrapper<SysRole>().inSql("id", "select role_id from sys_user_role where user_id=" + 1));
        user_id.forEach(System.out::println);
    }


    @Test
    void testList(){
        List<Long> longs = Arrays.asList(1L, 2L);
        longs.stream().forEach(System.out::println);
    }




    @Test
    void test12(){
        // 权限树构建
        List<SysMenu> menuList = menuService.list();
        HashMap<Long, SysMenu> queryMap = new HashMap<>();
        ArrayList<SysMenu> ans = new ArrayList<>();

        Stream<SysMenu> sorted = menuList.stream().sorted(new Comparator<SysMenu>() {
            @Override
            public int compare(SysMenu menu1, SysMenu menu2) {
                return (int) (menu2.getParentId() - menu1.getParentId());
            }
        });

        sorted.forEach(r->{
            queryMap.put(r.getId(), r);
        });

        Stream<SysMenu> sorted1 = menuList.stream().sorted(new Comparator<SysMenu>() {
            @Override
            public int compare(SysMenu menu1, SysMenu menu2) {
                return (int) (menu2.getParentId() - menu1.getParentId());
            }
        });

        sorted1.forEach(r->{
            if (queryMap.containsKey(r.getParentId())){
                SysMenu menu = queryMap.get(r.getParentId());
                menu.getChildren().add(r);
                queryMap.put(r.getParentId(), menu);
            }
            if (r.getParentId()==0L){
                ans.add(r);
            }
        });

        System.out.println(ans);

        //构建出了权限树
        printMenuTree(ans);

    }


    public void printMenuTree(List<SysMenu> menus){
        menus.stream().forEach(r->{
            if(r.getChildren().size()!=0){
                printMenuTree(r.getChildren());
            }else{
                System.out.println(r.getName());
            }
        });
    }

    @Test
    public void testBTree(){
        List<SysMenu> sysMenus = menuService.buildTreeMenu(menuService.list());
        System.out.println(sysMenus);
        printMenuTree(sysMenus);
    }




}
