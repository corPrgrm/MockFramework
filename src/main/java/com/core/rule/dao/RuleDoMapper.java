package com.core.rule.dao;

import com.core.rule.bean.dataObj.RuleDo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleDoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rule
     *
     * @mbg.generated Thu Nov 21 16:34:50 CST 2019
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rule
     *
     * @mbg.generated Thu Nov 21 16:34:50 CST 2019
     */
    int insert(RuleDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rule
     *
     * @mbg.generated Thu Nov 21 16:34:50 CST 2019
     */
    RuleDo selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rule
     *
     * @mbg.generated Thu Nov 21 16:34:50 CST 2019
     */
    List<RuleDo> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rule
     *
     * @mbg.generated Thu Nov 21 16:34:50 CST 2019
     */
    int updateByPrimaryKey(RuleDo record);
}