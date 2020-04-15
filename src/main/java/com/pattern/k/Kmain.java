package com.pattern.k;

/**    安全性 服务器端校验  Java源码安全审查:https://cloud.tencent.com/developer/article/1165105
 * 幂等：执行多次结果一致，且合理
 *
 *1.防重  支付系统的防重设计：https://yq.aliyun.com/articles/636421  查看完整的流程 补偿等
 *      0.背景：当服务端对于请求的响应涉及数据的修改，或状态的变更时，可能会造成极大的危害。重复请求的后果在交易系统、售后维权，以及支付系统中尤其严重。
                前台操作的抖动，快速操作，网络通信或者后端响应慢，都会增加后端重复处理的概率。
 *      1.前台
 *          1.token  cookie / session / token
 *          2.弹出确认界面
 *          3.disable入口并倒计时
 *      2.后台（高并发时的幂等性）  第三方支付前台回调和后台回调，第三方支付批量回调，慢性能业务逻辑（如用户提交退款申请，商家同意退货/退款等）或慢网络环境时，是重复处理的高发场景。
 *          1.幂等 查询类大多是幂等的
 *          2.基于DB中退款订单状态的验证
 *                  从DB查询出来的退款详情（包括状态）往往还可以用在后续逻辑中
 *                  优化的方向是减少查询到更新之间业务处理时间，可降低空档期的并发影响。极致情况下如果查询和更新变成了原子操作，则就不存在我们当前的问题。
 *          3.乐观锁:version  悲观锁:for update  bjbank 转账撤销和转账定时任务 避免从不同业务模块对同一笔进行操作
 *          4.基于缓存数据状态的验证
 *                   I.  每次退款发起申请，读取缓存中是否有以orderId为key的值
                     II. 没有，则往缓存中写入以orderId为key的value
                     III.有，则说明有该订单的退款正在进行。
                     IV. 操作完清缓存，或者缓存存值的时候设置生命周期

                但是仍然不是原子操作。插入和读取缓存还是有时间间隔。在极致的情况下还是存在重复操作的情况。
            5.利用唯一索引机制的验证  -- 流水号 流水号也可用于分布式定位，自己系统和调用外部系统等
                 CREATE TABLE `TradeLock` (
                 `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                 `type` int(11) NOT NULL COMMENT '锁类型',
                 `lockId` int(11) NOT NULL DEFAULT '0' COMMENT '业务ID',
                 `status` int(11) NOT NULL DEFAULT '0' COMMENT '锁状态',
                 PRIMARY KEY (`id`)
                 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Trade锁机制';
            6.基于缓存的计数器验证
                     由于数据库的操作比较消耗性能，了解到redis的计数器也是原子性操作。果断采用计数器。既可以提高性能，还不用存储，而且能提升qps的峰值。
                     每次request进来则新建一个以orderId为key的计数器，然后＋1。
                     如果>1（不能获得锁）: 说明有操作在进行，删除。如果=1（获得锁）: 可以操作。
            7.redis分布式锁

 *
 *2.顺序消费  -- 多线程对线程数取模 指定某个订单相关的消息都是由同一个线程处理  这样不仅仅使用了多线程，并且保证了顺序性
 *          0.背景：生产者指令：增 - 删 -改   mq:可以保证顺序性和至少消费一次：RocketMQ  一个topic下有多个队列，为了保证发送有序，RocketMQ提供了MessageQueueSelector队列选择机制Hash取模法，
 *                  让同一个订单发送到同一个队列中，再使用同步发送，只有同个订单的创建消息发送成功，再发送支付消息。这样，我们保证了发送有序。
 *          1.消费者如何保证顺序消费 是否可以在监听处理那里使用多线程呢？
 *              答：虽然接受到的消息是顺序的，但不能保证这里线程处理的速度和消息是一样的。解决：单线程 vs 消费完一个在发下一个 vs多线程对线程数取模 指定某个订单相关的消息都是由同一个线程处理。
 *                  发送应答(通用回复报文 系统内状态变化 岗位流程 大状态 )  vs   分布式如何返回上一个消费成功？-一个消费分布式事务
 *
 *3.分布式事务 - 事务 (事务是依赖数据库表锁、各种锁进行向上抽象的。commit..rollback... spring在这个基础上对编码事务进行配置和注解化)
 *               保证分布式系统数据一致性的6种方案：https://yq.aliyun.com/articles/140480
 *          0.背景：接着上面问题
 *              大家可以想一下，你下单流程可能涉及到10多个环节，你下单付钱都成功了，但是你优惠券扣减失败了，积分新增失败了，前者公司会被薅羊毛，后者用户会不开心，
 *              但是这些都在不同的服务怎么保证大家都成功呢？
 *          1.事务是恢复和并发控制的基本单位。事务应该具有4个属性：原子性、一致性、隔离性、持久性。这四个属性通常称为ACID特性。
 *          2.  2pc（两段式提交）
                 3pc（三段式提交）
                 TCC（Try、Confirm、Cancel）
                 最大努力通知
                 XA
                 本地消息表（ebay研发出的）
                 半消息/最终一致性（RocketMQ）
            3.2pc在两个系统操作事务的时候都锁定资源但是不提交事务，等两者都准备好了，告诉消息中间件，然后再分别提交事
              半消息/最终一致性（RocketMQ）：-- https://www.toutiao.com/i6808102511510028813/?tt_from=weixin&utm_campaign=client_share&wxshare_count=1&timestamp=1586915317&app=news_article&utm_source=weixin&utm_medium=toutiao_android&req_id=20200415094836010014047040170314F1&group_id=6808102511510028813
                        业务主动方本地事务提交失败，业务被动方不会收到消息的投递。
                        只要业务主动方本地事务执行成功，那么消息服务一定会投递消息给下游的业务被动方，并最终保证业务被动方一定能成功消费该消息（消费成功或失败，即最终一定会有一个最终态）。
            4.事务选择
                1.
                2.

 *4.事务补偿 - 超时重试、异常重试 、延时调度 db保存现场  - 架构：同目录下补偿平台.png
 *              0.背景  下单调用自己的createOrder B系统的支付 C系统的库存 D系统的积分... 补偿系统作为边缘系统对aop进行更高抽象(类似于监控)对分布式事务处理
 *              1.创建config/execute/spring/Tranditions  -- 重点是对spring 内部类 实现 特别是在自定义框架柔和时候 **** \aop\反射\注解
 *
 *5.避免重复消费 mq - 异步、解耦、消峰
 *          0.背景：电商支付成功，通知活动系统，优惠券，积分，库存....进行相应处理 -- 1.调用方法 2.调用对应子模块 dubbo  微服务  3.调用对应子系统 分布式 4.通过mq方式
 *          1.一般消息队列的使用，我们都是有重试机制的，就是说我下游的业务发生异常了，我会抛出异常并且要求你重新发一次。别的子系统同样监听该操作，导致其他系统重复操作
 *            重试是很正常的，服务的网络抖动，开发人员代码Bug，还有数据问题等都可能处理失败要求重发的。
 *          2.一般幂等，我会分场景去考虑
 *                  比如跟金钱相关的场景那就很关键呀，就做强校验，别不是很重要的场景做弱校验。  类似于上面防重的5通过加表记录处理。只不过这里引入了流水表。也解决了在金额表字段无需状态等。历史表
 *                  你监听到了去加GMV是不是要调用加钱的接口，那加钱接口下面再调用一个加流水的接口，两个放在一个事务，成功一起成功失败一起失败。
 *                  每次消息过来都要拿着订单号+业务场景这样的唯一标识（比如天猫双十一活动）去流水表查，看看有没有这条流水，有就直接return不要走下面的流程了，没有就执行后面的逻辑。
                    之所以用流水表，是因为涉及到金钱这样的活动，有啥问题后面也可以去流水表对账，还有就是帮助开发人员定位问题。

                 弱校验：
                 这个简单，一些不重要的场景，比如给谁发短信啥的，我就把这个id+场景唯一标识作为Redis的key，放到缓存里面失效时间看你场景，一定时间内的这个消息就去Redis判断。

 *6.加密 - 签名验签
 *          1.base64/md5/sha356....
 *          2.原理：
 *5.sql
 *
 */
public class Kmain {
}
