package com.pattern;

import org.springframework.stereotype.Service;

/**
 * @author YangWenjun
 * @date 2019/7/29 14:19
 * @project hook
 * @title: springFathrer
 * @description:
 */

@Service
public class springFathrer {

    private String  name  = "xx";

    static{
        System.out.println("static");
    }

    {
        System.out.println("structor kuai");
    }

    public springFathrer() {
        super();
        System.out.println("springFathrer");
    }

    public  void  abc(){
        System.out.println("abc");
    }
}
