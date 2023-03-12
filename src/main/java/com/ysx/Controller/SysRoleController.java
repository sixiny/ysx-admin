package com.ysx.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysx.common.constant.Constant;
import com.ysx.pojo.*;
import com.ysx.service.SysRoleMenuService;
import com.ysx.service.SysRoleService;
import com.ysx.util.RedisUtil;
import com.ysx.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/10/20:15
 * @Description:
 */
@CrossOrigin
@RestController
@RequestMapping("/sys/role")
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private SysRoleMenuService roleMenuService;

    @Autowired
    private RedisUtil redisUtil;

    // roleList
    @GetMapping("/listAll")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R listAll(){
        List<SysRole> roles = roleService.list();
        Map<String, Object> ans = new HashMap<>();
        ans.put("roleList", roles);
        return R.ok(ans);
    }

    /**
     * 分页查询
     */
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R list(@RequestBody PageBean pageBean){
        System.out.println("pageBean:"+pageBean);
        String query=pageBean.getQuery().trim();
        Page<SysRole> pageResult = roleService.page(new Page<>(pageBean.getPageNum(),pageBean.getPageSize()), new QueryWrapper<SysRole>().like(StringUtil.isNotEmpty(query), "name", query));
        List<SysRole> roleList = pageResult.getRecords();
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("roleList",roleList);
        resultMap.put("total",pageResult.getTotal());
        return R.ok(resultMap);
    }

    /**
     * 添加或者修改
     */
    @RequestMapping("/save")
    @PreAuthorize("hasAnyAuthority('system:role:add','system:role:edit')")
    public R addOrUpdate(@RequestBody SysRole role){
        if(role.getId()==null||role.getId()==-1){
            //添加
            role.setCreateTime(new Date());
            roleService.save(role);
        }else{
            role.setUpdateTime(new Date());
            roleService.update(role, new QueryWrapper<SysRole>().eq("id", role.getId()));
        }
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }


    /**
     * 根据id查找
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R selectRoleById(@PathVariable("id") Long id){
        SysRole role = roleService.getById(id);
        HashMap<String, Object> ans = new HashMap<>();
        ans.put("sysRole", role);
        return R.ok(ans);
    }

    /**
     *  删除权限
     */
    @Transactional
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public R deleteByIds(@RequestBody Long[] ids){
        roleService.removeByIds(Arrays.asList(ids));
        roleMenuService.remove(new QueryWrapper<SysRoleMenu>().in("role_id", ids));
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }



    /**
     * 根据角色id查询相关权限
     */
    @RequestMapping("/menus/{id}")
    @PreAuthorize("hasAuthority('system:role:menu')")
    public R getRoleMenus(@PathVariable("id") Long id){
        List<SysRoleMenu> roleMenus = roleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        List<Long> menuIds = roleMenus.stream().map(r -> r.getMenuId()).collect(Collectors.toList());
        return R.ok().put("menuIdList",menuIds);
    }


    /**
     * 更新角色分配权限
     */
    @Transactional
    @PostMapping("/updateMenus/{id}")
    @PreAuthorize("hasAuthority('system:role:menu')")
    public R updateMenus(@PathVariable("id") Long id, @RequestBody Long[] menuIds){
//        System.out.println(request.getRequestURI());
        ArrayList<SysRoleMenu> ans = new ArrayList<>();
        roleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        Arrays.stream(menuIds).forEach(r->{
            SysRoleMenu temp = new SysRoleMenu();
            temp.setRoleId(id);
            temp.setMenuId(r);
            ans.add(temp);
        });
        roleMenuService.saveBatch(ans);
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }


}
