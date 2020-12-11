package com.pattern.l;

/**
 *   0.总结
 *      0.宗旨：一定要在异常出现后，让别人知道，想要别人不知道，就必须自己解决，而不是假装没有
 *      1.什么时候进行返回值判断
 *              >= 0 存在是无的情况，那么就要判断是填默认值，还是xx.getxx.方法调用
 *              >0   返回值必须存在否则无法执行，则无需判断直接通过异常进行向外抛出
 *      2.接口定义是否预留throw Exception -->接口定义需要思考...
 *              1.很小记录失败就无需定义比如 insert.... select...  简单/稳定
 *              2.service且内部逻辑复杂(比如service只是简单的dao调用则无需) 则预留ServiceExcption定义
 *              3.单独模块调用，外部系统调用，这些异常都要看是否try..catch...
 *      3.捕获异常时机
 *              如果你知道如何处理，比如下面的记录，降级，转换，补偿，重试，那么给出捕获后给出方案。   <<<<<<<<<<<<<<<<<<<================
 *              否则，继续向外抛，知道应该到的地方。知道controller 异常切面统一处理
 *
 *   1.如何处理异常合理-极客
 *      1.Controller 层负责信息收集、参数校验、转换服务层处理的数据适配前端，轻业务逻辑；
             Service 层负责核心业务逻辑，包括各种外部服务调用、访问数据库、缓存处理、消息处理等；
             Repository 层负责数据访问实现，一般没有业务逻辑。

        2.Repository 层出现异常或许可以忽略，或许可以降级，或许需要转化为一个友好的异
             常。如果一律捕获异常仅记录日志，很可能业务逻辑已经出错，而用户和程序本身完全
             感知不到。
             Service 层往往涉及数据库事务，出现异常同样不适合捕获，否则事务无法自动回滚。此
             外 Service 层涉及业务逻辑，有些业务逻辑执行中遇到业务异常，可能需要在异常后转
             入分支业务流程。如果业务异常都被框架捕获了，业务功能就会不正常。
             如果下层异常上升到 Controller 层还是无法处理的话，Controller 层往往会给予用户友
             好提示，或是根据每一个 API 的异常表返回指定的异常类型，同样无法对所有异常一视
             同仁。

        3.除了通过日志正确记录异常原始信息外，通常还有
             转换，即转换新的异常抛出。对于新抛出的异常，最好具有特定的分类和明确的异常消
             息，而不是随便抛一个无关或没有任何信息的异常，并最好通过 cause 关联老异常。
             重试，即重试之前的操作。比如远程调用服务端过载超时的情况，盲目重试会让问题更
             严重，需要考虑当前情况是否适合重试。
             恢复，即尝试进行降级处理，或使用默认值来替代原始数据。

     2.异常传递信息vs返回值VS父类msg进行判断。commonvalidator。了解要的点
     3.异常到底捕获还是跑出。上层知道处理，没有默认值，严重
     4.将对其他模块调用返回进行太try。。cstch。全局只是兜底。代码异常处理还是老样子 VS 不允许捕获异常。全部到controller
         https://blog.csdn.net/dinglang_2009/article/details/93346333
         由于一堆健壮的try-catch和空判断，导致问题发现很晚，可能很小一个问题最后变成了一个大事件，在一些IT系统里面，尤其常见。
         更加不会出现前台返回成功，后台有异常什么也没有做的场景

         当存在空可以不处理，继续那也就是a.xx时才需要判空，如果是空一定不可以做。及是空return。不影响主流程。。如果需要调用着感知，则不判断能有啥报错。运行时异常直接往外抛
         https://zhuanlan.zhihu.com/p/29005176
     根据业务逻辑定于借口时，判断是否要抛出异常
 *
 */
public class exceptionPoint {
}
