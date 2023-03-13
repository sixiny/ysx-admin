package com.ysx.common.Filter;

import com.ysx.common.constant.JwtConstant;
import com.ysx.common.security.MyUserDetailServiceImpl;
import com.ysx.pojo.CheckResult;
import com.ysx.pojo.SysUser;
import com.ysx.service.SysUserService;
import com.ysx.util.JwtUtils;
import com.ysx.util.RedisUtil;
import com.ysx.util.StringUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: ysx
 * @Date: 2023/03/06/21:42
 * @Description: JWT token验证
 */
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    private SysUserService service;

    @Autowired
    private MyUserDetailServiceImpl myUserDetailService;

    //请求白名单
    private static final String URL_WHITELIST[] ={
            "/login",
            "/logout",
            "/captcha",
            "/password",
            "/image/**",
            "/test/**",
    } ;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader("token");
        if(StringUtil.isEmpty(token) || new ArrayList<String>(Arrays.asList(URL_WHITELIST)).contains(request.getRequestURI())){
            chain.doFilter(request, response);
            return;
        }

//        System.out.println("token验证通过");
        CheckResult checkResult = JwtUtils.validateJWT(token);
        if(!checkResult.isSuccess()){
            switch (checkResult.getErrCode()){
                case JwtConstant.JWT_ERRCODE_NULL: throw new JwtException("Token不存在");
                case JwtConstant.JWT_ERRCODE_FAIL: throw new JwtException("Token验证不通过");
                case JwtConstant.JWT_ERRCODE_EXPIRE: throw new JwtException("Token过期");
            }
        }

        Claims claims = JwtUtils.parseJWT(token);
        String username = claims.getSubject();
        SysUser user = service.getUserByName(username);
        //TODO 请求装配权限  每次请求前 呢 会去核对token合不合法 然后依靠token解析获取用户信息 配置相关权限
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), myUserDetailService.getauthorities(user.getId()));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        chain.doFilter(request, response);

    }
}
