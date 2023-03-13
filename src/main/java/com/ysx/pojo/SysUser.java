package com.ysx.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 
 * @TableName sys_user
 */
@ApiModel("用户实体类")
@TableName(value ="sys_user")
@Data
public class SysUser extends BaseEntity implements Serializable {


    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空") //搭配 接受前端数据是@Valid注解
    private String username;

    /**
     * 密码  接受到的是加密的
     */
    private String password;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 所属角色 多个角色之间，隔开
     */
    @TableField(exist = false)
    private String roles;

    /**
     *旧密码
     */
    @ApiModelProperty("更新密码时寄存旧密码(未加密)")
    @TableField(exist = false)
    private String oldPassword;

    /**
     * 新密码
     */
    @ApiModelProperty("更新密码时寄存新密码")
    @TableField(exist = false)
    private String newPassword;

    /**
     * 用户角色
     */
    @TableField(exist = false)
    private List<SysRole> sysRoleList;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}