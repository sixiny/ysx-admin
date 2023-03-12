package com.ysx.service;

import com.ysx.pojo.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author YSX
* @description 针对表【sys_user】的数据库操作Service
* @createDate 2023-03-05 20:16:59
*/
public interface SysUserService extends IService<SysUser> {

    public SysUser getUserByName(String name);


    public String getUserAuthortyInfo(Long userId);
}
