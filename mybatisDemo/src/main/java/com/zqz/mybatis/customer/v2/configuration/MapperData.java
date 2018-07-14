package com.zqz.mybatis.customer.v2.configuration;

import java.io.Serializable;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class MapperData implements Serializable{

    private String statementSql;
    private Class resultType;
    private CRUDEnum crudEnum;

    public String getStatementSql() {
        return statementSql;
    }

    public void setStatementSql(String statementSql) {
        this.statementSql = statementSql;
    }

    public Class getResultType() {
        return resultType;
    }

    public void setResultType(Class resultType) {
        this.resultType = resultType;
    }

    public CRUDEnum getCrudEnum() {
        return crudEnum;
    }

    public void setCrudEnum(CRUDEnum crudEnum) {
        this.crudEnum = crudEnum;
    }
}
