package com.pattern.k;

import boot.SpringBootApplication;
import com.core.rule.bean.dataObj.DraftDo;
import com.core.rule.bean.dataObj.RuleDo;
import com.core.rule.dao.DraftDoMapper;
import com.core.rule.dao.RuleDoMapper;
import com.msg.Amq.AmqSender;
import com.pattern.k.execute.IDelegateOrderCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 事务选择
 * 事务补偿验证
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApplication.class)
public class Transactions {

        @Autowired
        private RuleDoMapper ruleDoMapper;
        @Autowired
        private DraftDoMapper draftDoMapper;
        @Autowired
        private IDelegateOrderCenter delegateOrderCenter;

    /**
     * 事务选择
     * @throws Exception
     *
     *
     *
     * -----事务----两个及以上落库操作-----问题：1.嵌套 失效  2.同一个方法中事务调用 ----------
            Spring中七种Propagation类的事务属性详解：

            REQUIRED：支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。
            关于业务的。都是一个整体的。配置许多是命名时，每个人不同，所以配置多。拦截器统一 vs 方法配置

            REQUIRES_NEW：新建事务，如果当前存在事务，把当前事务挂起。
            启动一个新的, 不依赖于环境的 "内部" 事务. 这个事务将被完全 commited 或 rolled back 而不依赖于外部事务, 它拥有自己的隔离范围, 自己的锁, 等等. 当内部事务开始执行时, 外部事务将被挂起, 内务事务结束时, 外部事务将继续执行.

            只要自己没有问题，自己就commit，即使外部事务异常也不会回滚。如果自己rollback,也不影响外部的。

            addLog操作 - 记库 IserviceDao 抽离哪些信息记库  vs  文件

            SUPPORTS：支持当前事务，如果当前没有事务，就以非事务方式执行。
            NESTED：支持当前事务，如果当前事务存在，则执行一个嵌套事务，如果当前没有事务，就新建一个事务。
            PROPAGATION_NESTED 开始一个 "嵌套的" 事务,它是已经存在事务的一个真正的子事务. 潜套事务开始执行时,? 它将取得一个 savepoint. 如果这个嵌套事务失败, 我们将回滚到此 savepoint. 潜套事务是外部事务的一部分, 只有外部事务结束后它才会被提交.?
            由此可见, PROPAGATION_REQUIRES_NEW 和 PROPAGATION_NESTED 的最大区别在于, PROPAGATION_REQUIRES_NEW 完全是一个新的事务, 而 PROPAGATION_NESTED 则是外部事务的子事务, 如果外部事务 commit, 潜套事务也会被 commit, 这个规则同样适用于 roll back.?

            NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。

            MANDATORY：支持当前事务，如果当前没有事务，就抛出异常。
            NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。

             @Transactional(propagation=Propagation.REQUIRED,readOnly=true)//只读，不能更新，删除
             @Transactional(propagation=Propagation.REQUIRED,timeout=30)//超时30秒
             其实propagation=Propagation.REQUIRED 不影响。所以可以看作是默认吧。写不写都行

             readonly  隔离级别  https://blog.csdn.net/KimSoft/article/details/5280271
             如果你一次执行单条查询语句，则没有必要启用事务支持，数据库默认支持SQL执行期间的读一致性；
             如果你一次执行多条查询语句，例如统计查询，报表查询，在这种场景下，多条查询SQL必须保证整体的读一致性，否则，在前条SQL查询之后，后条SQL查询之前，数据被其他用户改变，则该次整体的统计查询将会出现读数据不一致的状态，此时，应该启用事务支持。

             只配置readOnly,那么默认的传播行为是什么？
             不同数据库默认的隔离级别，配置隔离级别。 哪些情况需要配置读未提交....

             resetInterest:事务 never

             幻读：事务在插入已经检查过不存在的记录时，惊奇的发现这些数据已经存在了，之前的检测获取到的数据如同鬼影一般
             https://blog.csdn.net/liuxiao723846/article/details/51817825


             Oracle默认的隔离级别为Read Committed，因此可能出现不可重复度和幻读。
             MySql默认的隔离级别为Repeatable Read，因此只会出现幻读的情况。

             幻读和不可重复读都是读取了另一条已经提交的事务（这点就脏读不同），所不同的是不可重复读查询的都是同一个数据项，
             而幻读针对的是一批数据整体（比如数据的个数）。

             为什么互联网使用读已提交？https://www.cnblogs.com/shoshana-kong/p/10516404.html

             隔离级别和readonly 修饰 qry.... get .. search ...等查询方法，进行设值和优化   @Transactional(readonly = true)

             transaction - 关联数据库级别的特性
             1.@Transactional 只能应用到 public 方法才有效
             2.避免 Spring 的 AOP 的自调用问题
             1.transaction 如何关联aop
             2.如何从库的事务中封装
             3.orm session关系
              ****锁和数据库隔离级别(***知乎上一个关于 锁/隔离级别/ 现有问题 后有解答  但是当前还是没有找到****)
             spring事务源码对几个属性是如何控制的呢  隔离级别 - 物理隔离级别 - 锁 /传播行为
             传播行为：https://www.cnblogs.com/shoshana-kong/p/10712834.html
             锁和隔离级别:https://tech.meituan.com/2014/08/20/innodb-lock.html
             spring传播行为和Conn如何关联隔离级别的物理
             Conn中包装了。有个个驱动实现

             https://blog.csdn.net/BuquTianya/article/details/78946473
             https://www.jianshu.com/p/1becdc376f5d

     */
    @Test
        @Transactional
        public void test() throws Exception {
            RuleDo ruleDo = new RuleDo();
            ruleDo.setDraftno("12345");
            ruleDo.setProperty("12345");
            ruleDo.setIsregex(1);
            ruleDo.setIsjsengine(1);
            ruleDo.setIsempty(1);
            ruleDo.setLength(1);
            ruleDo.setType("123456");
            DraftDo draftDo = new DraftDo();
            draftDo.setDrafttemplate("121212".getBytes());
            draftDo.setDraftdescribe("1221");
            draftDo.setDraftno("12345");

            ruleDoMapper.insert(ruleDo);
            System.out.println("ruleDo inserted");
            if(1==1) {
                throw new Exception("eeee");
            }
            draftDoMapper.insert(draftDo);
            System.out.println("draftDo inserted");
        }

    /***
     * 验证补偿
     * @throws Exception
     *
     *
     *
     */
    @Test
    @Transactional
    public void test2() throws Exception {

        DraftDo draftDo = new DraftDo();
        draftDo.setDrafttemplate("121212".getBytes());
        draftDo.setDraftdescribe("1221");
        draftDo.setDraftno("12345");

        draftDoMapper.insert(draftDo);
        System.out.println("drfatDo inserted");
        delegateOrderCenter.notifyOrder(draftDo);
    }


}
