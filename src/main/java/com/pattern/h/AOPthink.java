package com.pattern.h;

/**
 *
 *  1.事务、日志、统一异常 vs aop 前者是行为正确的要素，后者是实现方式；没有aop同样可以，只是有了aop将主次逻辑分开，抽象为对应的切面处理，关注主逻辑。
 *  2.aop底层是代理 ,动态代理（jdk-继承*cglib-接口#aspect 字节码 asm）
         切面是将前置，后置校验代码逻辑有了主次之分，比如日志，返回值，加解密。。
    3.代理不仅仅只是aop,同样是一种思想。即：代理模式
        代理这是主流程中的重对象。或者说模块对象。比如webserce....
    4.为什么我们写不出框架级别的代码
            1.模式是对大型的组织，而大型需要业务理解+模型注意事项全面要素+大型复杂对象构建(抽象层次、维度 、粒度 六大原则 )+可扩展(设计模式)
            2.设计模式就是将方法抽成类。比如前置判断、返回保证，hashmap....
            3.其实你写的类中已经实现了这些，只是没有抽成类。因为简单
            4.spring将大的架子打起来，比如切面...listener。。。学习分类。商业化
            5.设计模式重要，但是解决问题更是关键。比如加密正确再去找合适位置去放，比如aop\抽象成类
 *
 */
public class AOPthink {
}