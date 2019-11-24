package com.busi.seckill.dao;

import com.secKill.dataObj.UserPasswordDo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordDoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Sat Apr 13 19:02:20 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Sat Apr 13 19:02:20 CST 2019
     */
    int insert(UserPasswordDo record);
    int insertSelective(UserPasswordDo record);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Sat Apr 13 19:02:20 CST 2019
     */
    UserPasswordDo selectByPrimaryKey(Integer id);
    UserPasswordDo selectByUserId(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Sat Apr 13 19:02:20 CST 2019
     */
    List<UserPasswordDo> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Sat Apr 13 19:02:20 CST 2019
     */
    int updateByPrimaryKey(UserPasswordDo record);
}