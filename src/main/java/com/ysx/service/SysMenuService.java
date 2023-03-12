package com.ysx.service;

import com.ysx.pojo.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author YSX
* @description 针对表【sys_menu】的数据库操作Service
* @createDate 2023-03-07 11:15:08
*/
public interface SysMenuService extends IService<SysMenu> {

    public List<SysMenu> buildTreeMenu(List<SysMenu> menus);
}
