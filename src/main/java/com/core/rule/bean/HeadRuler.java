package com.core.rule.bean;

import java.util.Map;

/**
 * @author YangWenjun
 * @date 2019/11/1 16:59
 * @project MockFramework
 * @title: Ruler
 * @description:
 *          规则引擎：是用于业务灵活 、动态配置if/else .， 所以这里是无需的。将业务逻辑注入应用程序  --- 借鉴
 *          现状:对传入值，类型，等进行schema级校验
 *          Parttern.compile()
 *
 *          对中断分支定义的对象。所有的链前面都需要组装该对象特性  vs baseRuler
 */
public class HeadRuler {

    private boolean isTimeOut ;
    private boolean isRepeat  ;

    /**
     *  通过“构造” 进行相关初始化  默认值/其他init()
     */
    public HeadRuler() {
        this.isTimeOut = false;
        this.isRepeat = false;
        //....
    }

    public Map<Boolean,String> volidate(){


        /**
         * aop
         * 反射( 动态获取当时对象的所有信息 ) + T
         * 责任链
         */



        return  null ;
    }

}
