package com.ysx.Controller;



import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ysx.common.constant.Constant;
import com.ysx.pojo.*;
import com.ysx.service.SysRoleService;
import com.ysx.service.SysUserRoleService;
import com.ysx.service.SysUserService;
import com.ysx.util.DateUtil;
import com.ysx.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.internal.org.objectweb.asm.Handle;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.rmi.MarshalledObject;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/08/21:01
 * @Description:  用户管理controller
 */
@Api("用户管理")
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private SysUserRoleService userRoleService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Value("${avatarImagesFilePath}")
    private String avatarImagesFilePath;

    @Value("${commonPassowrd}")
    private String commonPassword;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 用户修改或者添加信息
     * @return
     */
    @ApiOperation("添加或修改用户信息")
    @RequestMapping("/save")
    @PreAuthorize("hasAnyAuthority('system:user:add','system:user:edit')")
    public R addOrEditUser(@RequestBody SysUser user){
        if(user.getId()==null || user.getId()==-1){
            user.setCreateTime(new Date());
            user.setPassword(encoder.encode(user.getPassword()));
            userService.save(user);
        }else{
            //修改
            user.setUpdateTime(new Date());
            // 根据updateWrapper数据更新数据
            userService.update(user, new UpdateWrapper<SysUser>().eq("id", user.getId()));
        }
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }

    /**
     * 修改密码
     * @param user
     * @return
     */
    @ApiOperation("修改密码")
    @PostMapping("/updateUserPwd")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R updateUserPwd(@RequestBody SysUser user){
        SysUser olduser = userService.getById(user.getId());
        if(encoder.matches(user.getOldPassword(), olduser.getPassword())){
            //匹配gengxin
            olduser.setPassword(encoder.encode(user.getNewPassword()));
            olduser.setUpdateTime(new Date());
            userService.update(olduser, new UpdateWrapper<SysUser>().eq("id", olduser.getId()));
            return R.ok();
        }else{
            return R.error("旧密码错误");
        }
    }

    /**
     * 根据id  查询  修改用户时使用
     * @param id
     * @return
     */
    @ApiOperation("根据id查询用户")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R findUserById(@PathVariable("id")Integer id){
        SysUser user = userService.getById(id);
        Map<String, Object> ans = new HashMap<>();
        ans.put("sysUser", user);
        return R.ok(ans);
    }

    /**
     * 验证用户名  新建用户时验证用户名是否可用
     */
    @ApiOperation("验证用户名是不是已经存在")
    @PostMapping("/checkUserName")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R checkUserName(@RequestBody SysUser user){
        if (userService.getUserByName(user.getUsername())==null){
            return R.ok();
        }else{
            return R.error();
        }
    }

    /**
     *  删除用户
     */
    @ApiOperation("删除用户")
    @Transactional
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public R deleteByIds(@RequestBody Long[] ids){
        userService.removeByIds(Arrays.asList(ids));
        userRoleService.remove(new QueryWrapper<SysUserRole>().in("user_id", ids));
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }


    /**
     * 密码重置
     */
    @ApiOperation("重置密码")
    @GetMapping("/resetPassword/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R resetPassword(@PathVariable("id") Long id){
        SysUser user = userService.getById(id);
        user.setPassword(encoder.encode(commonPassword));
        userService.update(user, new UpdateWrapper<SysUser>().eq("id", id));
        return R.ok();
    }

    /**
     * 更新statues
     */
    @ApiOperation("更新用户状态")
    @GetMapping("/updateStatus/{id}/status/{status}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R updateStatus(@PathVariable("id") Long id, @PathVariable("status") String status){
        SysUser user = userService.getById(id);
        user.setStatus(status);
        userService.saveOrUpdate(user);
        return R.ok();
    }

    /**
     * 上传用户头像图片
     * @param file
     * @return
     * @throws Exception
     */
    @ApiOperation("上传用户头像")
    @RequestMapping("/uploadImage")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Map<String,Object> uploadImage(MultipartFile file)throws Exception{
        Map<String,Object> resultMap=new HashMap<>();
        if(!file.isEmpty()){
            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            String suffixName=originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName= DateUtil.getCurrentDateStr()+suffixName;
            FileUtils.copyInputStreamToFile(file.getInputStream(),new File(avatarImagesFilePath+newFileName));
            resultMap.put("code",0);
            resultMap.put("msg","上传成功");
            Map<String,Object> dataMap=new HashMap<>();
            dataMap.put("title",newFileName);
            dataMap.put("src","image/userAvatar/"+newFileName);
            resultMap.put("data",dataMap);
        }
        return resultMap;
    }

    /**
     * 修改用户头像
     * @param sysUser
     * @return
     */
    @ApiOperation("修改头像")
    @RequestMapping("/updateAvatar")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R updateAvatar(@RequestBody SysUser sysUser){
        SysUser currentUser = userService.getById(sysUser.getId());
        currentUser.setAvatar(sysUser.getAvatar());
        userService.updateById(currentUser);
        return R.ok();
    }


    /**
     *  根据条件分页查询
     */
    @ApiOperation("用户分页查询")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R list(@RequestBody PageBean pageBean){
        String name = pageBean.getQuery().trim();
        Page<SysUser> page = userService.page(new Page<>(pageBean.getPageNum(), pageBean.getPageSize()), new QueryWrapper<SysUser>().like(!StringUtils.isEmpty(name), "username", name));
        List<SysUser> users = page.getRecords();
        for (SysUser user : users) {
            List<SysRole> roles = roleService.list(new QueryWrapper<SysRole>().inSql("id", "select role_id from sys_user_role where user_id=" + user.getId()));
            user.setSysRoleList(roles);
        }
        Map<String, Object> ans = new HashMap<>();
        ans.put("userList", users);
        ans.put("total", page.getTotal());
        return R.ok(ans);
    }


    /**
     * 用戶授權 給用戶分配權限
     */
    @ApiOperation("给用户分配权限后 更新权限")
    @Transactional
    @PostMapping("/grantRole/{userId}")
    @PreAuthorize("hasAuthority('system:user:role')")
    public R grantUser(@PathVariable("userId") Long userId, @RequestBody Long[] roleId){
        ArrayList<SysUserRole> userRoleList = new ArrayList<>();
        Arrays.stream(roleId).forEach(r->{
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(r);
            sysUserRole.setUserId(userId);
            userRoleList.add(sysUserRole);
        });
//         userRoleService.list(new Qu)
//        userRoleService.removeByIds()
        userRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        userRoleService.saveBatch(userRoleList);
        redisUtil.removeByPrex(Constant.AUTHORITY_KEY);
        return R.ok();
    }
}
