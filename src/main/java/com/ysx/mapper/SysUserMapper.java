package com.ysx.mapper;

import com.ysx.pojo.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author YSX
* @description 针对表【sys_user】的数据库操作Mapper
* @createDate 2023-03-05 20:16:59
* @Entity com.ysx.pojo.SysUser
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser getUserByInputName(String name);
}




