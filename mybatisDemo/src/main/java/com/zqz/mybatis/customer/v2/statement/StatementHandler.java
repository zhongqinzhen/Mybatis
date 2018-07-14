package com.zqz.mybatis.customer.v2.statement;

import com.zqz.mybatis.customer.common.model.Tag;
import com.zqz.mybatis.customer.v2.resultset.ResultSetHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class StatementHandler {

    private ResultSetHandler resultSetHandler;

    public StatementHandler() {
        this.resultSetHandler = new ResultSetHandler();
    }

    public <T> List<T> query(String statementSql, String parameter, Class resultType){

        Connection connection = getConnection();
        Statement statement = getStatement(connection);

        List<T> tlist = new ArrayList<>();
        try {

            ResultSet resultSet = statement.executeQuery(String.format(statementSql, Integer.parseInt(parameter)));

            tlist.add((T) resultSetHandler.handler(resultSet,resultType));

            return tlist;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            closeConnection(connection);

            closeStatement(statement);
        }

        return null;
    }

    public int execute(String statementSql,Object parameter){
        Connection connection = getConnection();
        Statement statement = getStatement(connection);

        Tag tag = (Tag) parameter;

        try {
            return statement.executeUpdate(String.format(statementSql, tag.getTagName(), tag.getTagType()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Statement getStatement(Connection connection){
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void closeStatement(Statement statement){
        if (statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Connection getConnection(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/parttime";
        String user = "root";
        String pwd = "zhongqinzhen";

        try {

            Class.forName(driver);

            Connection connection = DriverManager.getConnection(url, user, pwd);
            return connection;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private void closeConnection(Connection conn){
        if (conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
