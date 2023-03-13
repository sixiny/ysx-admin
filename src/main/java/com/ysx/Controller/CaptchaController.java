package com.ysx.Controller;


import com.google.code.kaptcha.Producer;
import com.ysx.common.constant.Constant;
import com.ysx.pojo.R;
import com.ysx.util.RedisUtil;
import com.ysx.pojo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码Controller控制器
 */
@Api("验证码控制器")
@RestController
public class CaptchaController {

    @Autowired
    private Producer producer;

    @Autowired
    private RedisUtil redisUtil;


    @ApiOperation(value="captcha", notes="生成验证码")
    @GetMapping("/captcha")
    public R captcha() throws IOException {
        String key= UUID.randomUUID().toString(); // 生成随机唯一key
        String code = producer.createText();
        System.out.println("code="+code);
        BufferedImage image = producer.createImage(code);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        String str = "data:image/jpeg;base64,";
        String base64Img = str + encoder.encode(outputStream.toByteArray());
        redisUtil.hset(Constant.CAPTCHA_KEY,key,code,60*5);

        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("base64Img",base64Img);
        resultMap.put("uuid",key);
        return R.ok(resultMap);

    }

}
