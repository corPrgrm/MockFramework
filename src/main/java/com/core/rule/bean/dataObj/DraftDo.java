package com.core.rule.bean.dataObj;

public class DraftDo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column draft.id
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column draft.draftNo
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    private String draftno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column draft.draftDescribe
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    private String draftdescribe;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column draft.draftTemplate
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    private byte[] drafttemplate;


    public DraftDo(){}
    public DraftDo(String draftno, String draftdescribe, byte[] drafttemplate) {
        this.draftno = draftno;
        this.draftdescribe = draftdescribe;
        this.drafttemplate = drafttemplate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column draft.id
     *
     * @return the value of draft.id
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */



    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column draft.id
     *
     * @param id the value for draft.id
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column draft.draftNo
     *
     * @return the value of draft.draftNo
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public String getDraftno() {
        return draftno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column draft.draftNo
     *
     * @param draftno the value for draft.draftNo
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public void setDraftno(String draftno) {
        this.draftno = draftno == null ? null : draftno.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column draft.draftDescribe
     *
     * @return the value of draft.draftDescribe
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public String getDraftdescribe() {
        return draftdescribe;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column draft.draftDescribe
     *
     * @param draftdescribe the value for draft.draftDescribe
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public void setDraftdescribe(String draftdescribe) {
        this.draftdescribe = draftdescribe == null ? null : draftdescribe.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column draft.draftTemplate
     *
     * @return the value of draft.draftTemplate
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public byte[] getDrafttemplate() {
        return drafttemplate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column draft.draftTemplate
     *
     * @param drafttemplate the value for draft.draftTemplate
     *
     * @mbg.generated Fri Nov 22 09:03:32 CST 2019
     */
    public void setDrafttemplate(byte[] drafttemplate) {
        this.drafttemplate = drafttemplate;
    }
}