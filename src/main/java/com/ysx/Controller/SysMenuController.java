package com.ysx.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ysx.common.constant.Constant;
import com.ysx.pojo.R;
import com.ysx.pojo.SysMenu;
import com.ysx.service.SysMenuService;
import com.ysx.service.SysRoleService;
import com.ysx.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/10/21:40
 * @Description:
 */
@Api("权限菜单管理")
@RestController
@RequestMapping("/sys/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 查询所有菜单树信息
     * @return
     */
    @ApiOperation("获取权限树")
    @RequestMapping("/treeList")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R treeList(){
        // 查询所有菜单信息
        List<SysMenu> menuList = menuService.list(new QueryWrapper<SysMenu>().orderByAsc("order_num"));
        return R.ok().put("treeMenu", menuService.buildTreeMenu(menuList));
    }


    /**
     * 根据id查询
     * @param id
     * @return
     */
    @ApiOperation("根据id查询权限")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public R findById(@PathVariable Long id){
        SysMenu sysMenu = menuService.getById(id);
        return R.ok().put("sysMenu", sysMenu);
    }

    /**
     * 添加或者修改
     * @param sysMenu
     * @return
     */
    @ApiOperation("添加或修改权限信息")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('system:menu:add')"+"||"+"hasAuthority('system:menu:edit')")
    public R save(@RequestBody SysMenu sysMenu){
        if(sysMenu.getId()!=null && sysMenu.getId()!=-1){
            // 修改
            sysMenu.setUpdateTime(new Date());
            menuService.update(sysMenu, new UpdateWrapper<SysMenu>().eq("id", sysMenu.getId()));
        }else{
            // cunxinde
            sysMenu.setCreateTime(new Date());
            menuService.save(sysMenu);
        }
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }



    /**
     * 删除
     * @param id
     * @return
     */
    @ApiOperation("根据id删除权限")
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public R delete(@PathVariable Long id){
        int count = menuService.count(new QueryWrapper<SysMenu>().eq("parent_id", id));
        if(count>0){
            return R.error("请先删除子菜单！");
        }
        menuService.removeById(id);
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }



}
