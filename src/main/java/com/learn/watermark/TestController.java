package com.learn.watermark;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: watermark
 * @description:
 * @author: Martin Fowler
 * @create: 2024-10-21 08:59
 */

@RestController
public class TestController {

    @RequestMapping("/test")
    public String test(){
        return "success";
    }
}