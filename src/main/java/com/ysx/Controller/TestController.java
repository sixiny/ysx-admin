package com.ysx.Controller;

import com.ysx.pojo.R;
import com.ysx.pojo.SysUser;
import com.ysx.service.SysUserService;
import com.ysx.util.JwtUtils;
import com.ysx.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.util.resources.cldr.zh.CalendarData_zh_Hans_SG;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/05/20:17
 * @Description:
 */
@RestController
public class TestController {

    @Autowired
    private SysUserService service;

    @RequestMapping("/test/user/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public R getUsers(@RequestHeader(required = false) String token, HttpServletRequest request){
        System.out.println(request.getHeader("token"));
        if(!StringUtil.isEmpty(token)){
            Map<String, Object> ans = new HashMap<>();
            List<SysUser> list = service.list();
            ans.put("userlist", list);
            return R.ok(ans);
        }else{
            return R.error(401, "token为空");
        }
    }

//    @RequestMapping("/login")
//    public R testToken(){
//        return R.ok().put("token", JwtUtils.genJwtToken("ysx"));
//    }
}
