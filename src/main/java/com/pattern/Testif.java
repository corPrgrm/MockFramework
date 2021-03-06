package com.pattern;

/**
 * 
 *  @author  YangWenjun																																												flow
 *  @date   2018年10月6日																																								   case1              case 2  .......
 *  @description                																																							case1-1    ..................                        -->这种结构是易出错的，正确见：如何做2
 *                                       流控制语句：条件分支 {  if  / switch }              																										      ...								   将条件成立与否都表示出来
 *                                       存在于方法中																																						    flow
 *                                       
 *                                     1.  是什么            树形结构 ，脑图：类似于linux的目录结构，                                                                                            
 *                                     2.  为什么            对不同情况做出不同动作
 *                                     3. 如何做
 *                                                 1. if条件是正向还是反向    ==    /   ！=
 *                                                 2. 嵌套 (if - if   if - for)                 查看对应三种脑图  详见：https://www.cnblogs.com/dianqijiaodengdai/p/6141949.html ***
 *                                                                   1.if                            合理条件判断条件，即 == vs  ！= 哪种情况少上哪种       这是在业务逻辑之后考虑的，比如：if（a != null && a.size > 0 && a != ''）.....  
 *                                                                   2.if .. elseif ....            合理条件判断顺序，使其更早的过滤掉，增加击中
 *                                                                   3.if(){ if ....}                 条件1，2之间是否有代码块作为前置条件，也是条件1满足后默认给定的资源
 *                                                                 其实这里的if-for，也就是脑图中的一个”代码块“
 *                                                 3. 业务复杂中如何不乱  
 *                                                        			1.明确业务，明确分支点条件
 *                                                                  2.没写一个分支 就将是/否两个条件代码写好    这个框先搭好，否则后面就乱了
 *                                                 4. 重构简化逻辑
 *                                                                  1.if(){if(){}} 当if后直接再次if，中间没有任何操作   --> if(  && )
 *                                                                  2.当多个条件只需满足一个                                 --> if(   ||    )
 *                                                                代码中都是使用逻辑与/或  & vs &&     相同点：二者都满足     不同点：后者在第一个条件成立后才判断第二个，第一个不成立无须判断第二个。--顺序。                                   
 *                                        4.对比 原理  改进 
 *                                                   1.switch
 *                                                                  1.switch判断的类型有：byte、short、int、char（JDK1.6）以及它们的包装类（需要Java5.0/1.5以上版本支持），还有枚举类型，但是在JDK1.7后添加了对String类型的判断
 *                                                                                                    long、float、double、boolean四种基本类型以及它们的包装类（在Java所有版本中）  都不能用于switch语句。  
 *                                                                  2.case语句中少写了break，编译不会报错，但是会一直执行之后所有case条件下的语句而不再判断，直到default语句.注意 ：  break ;  case后买你跟常量(枚举获取到的就是常量)格式。
 *                                                                  3.若果没有符合条件的case就执行default下的代码块，default并不是必须的，也可以不写
 *                                                                      method(){
 *                                                                         String s  = ”abc“ ;
 *                                                                         switch(s){
 *                                                                           case(a):
 *                                                                             ....do....
 *                                                                             break ;
 *                                                                            case(abc):
 *                                                                             ...do...
 *                                                                             break ;
 *                                                                         }
 *                                                                      }
                                                           switch vs if
                                                                  switch 用作分支情况较多的判断，判断" 条件类型单一，只有一个入口 "，在分支执行完后（如果没有break跳出）不加判断的执行下去，
                                                                  if 嵌套的分支主要适合于分支情况较少的分支结构，判断类型不是单一 只要一个分支被执行后，后边分支的不加选择的跳过 
 *                                                   2.JAVA的if用法，比如if(...){} 和if()没有大括号直接写下面的区别是什么 
 *                                                                 没有大括号的时候 只有下面的一句归if管，   通常if() return ;{   break ,continue 分支中二者没有意义   }      
  *  @todo  TODO
 */
public class Testif {

}
