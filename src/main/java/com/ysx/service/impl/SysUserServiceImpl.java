package com.ysx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysx.common.constant.Constant;
import com.ysx.mapper.SysMenuMapper;
import com.ysx.mapper.SysRoleMapper;
import com.ysx.pojo.SysMenu;
import com.ysx.pojo.SysRole;
import com.ysx.pojo.SysUser;
import com.ysx.service.SysUserService;
import com.ysx.mapper.SysUserMapper;
import com.ysx.util.RedisUtil;
import com.ysx.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author YSX
* @description 针对表【sys_user】的数据库操作Service实现
* @createDate 2023-03-05 20:16:59
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public SysUser getUserByName(String name) {
        return sysUserMapper.getUserByInputName(name);
    }

    @Override
    public String getUserAuthortyInfo(Long userId) {
        StringBuffer authority=new StringBuffer();
        if(redisUtil.hasKey(Constant.AUTHORITY_KEY+String.valueOf(userId))){
//            System.out.println("有缓存");
            authority.append(redisUtil.get(Constant.AUTHORITY_KEY,String.valueOf(userId)));
        }else{
//            System.out.println("没有缓存");
            // 根据用户id获取所有的角色信息
            List<SysRole> roleList = roleMapper.selectList(new QueryWrapper<SysRole>().inSql("id", "SELECT role_id FROM sys_user_role WHERE user_id=" + userId));
            if(roleList.size()>0){
                String roleCodeStrs = roleList.stream().map(r -> "ROLE_" + r.getCode()).collect(Collectors.joining(","));
                authority.append(roleCodeStrs);
            }
            // 遍历所有的角色，获取所有菜单权限 而且不重复
            Set<String> menuCodeSet=new HashSet<>();
            for(SysRole sysRole:roleList){
                List<SysMenu> sysMenuList = menuMapper.selectList(new QueryWrapper<SysMenu>().inSql("id", "SELECT menu_id FROM sys_role_menu WHERE role_id=" + sysRole.getId()));
                for(SysMenu sysMenu:sysMenuList){
                    String perms=sysMenu.getPerms();
                    if(StringUtil.isNotEmpty(perms)){
                        menuCodeSet.add(perms);
                    }
                }
            }
            if(menuCodeSet.size()>0){
                authority.append(",");
                String menuCodeStrs = menuCodeSet.stream().collect(Collectors.joining(","));
                authority.append(menuCodeStrs);
            }
            redisUtil.set(Constant.AUTHORITY_KEY,String.valueOf(userId),authority,30*60);
        }
//        System.out.println("authority:"+authority.toString());
        return authority.toString();
    }
}




