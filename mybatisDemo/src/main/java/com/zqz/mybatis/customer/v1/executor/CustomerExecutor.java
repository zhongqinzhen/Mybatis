package com.zqz.mybatis.customer.v1.executor;

import com.zqz.mybatis.customer.common.model.Tag;

import java.sql.*;

/**
 * Created by zhongqinzhen on 2018/7/11.
 */
public class CustomerExecutor {

    public <T> T selectOne(String sqlStatement, String parameter) {


        Connection connection = getConnection();
        Statement statement = getStatement(connection);

        try {

            ResultSet resultSet = statement.executeQuery(String.format(sqlStatement, Integer.parseInt(parameter)));

            if (resultSet.next()){
                Tag tag = new Tag();
                tag.setId(resultSet.getLong("id"));
                tag.setTagName(resultSet.getString("tag_name"));
                tag.setTagType(resultSet.getString("tag_type"));
                tag.setCreateTime(resultSet.getTimestamp("create_time"));
                tag.setUpdateTime(resultSet.getTimestamp("update_time"));
                return (T) tag;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            closeConnection(connection);

            closeStatement(statement);
        }

        return null;
    }

    public int insert(String sqlStatement, Object parameter) {

        if (sqlStatement == null || parameter == null || !(parameter instanceof Tag)){
            return 0;
        }

        Connection connection = getConnection();
        Statement statement = getStatement(connection);

        Tag tag = (Tag) parameter;

        try {
            return statement.executeUpdate(String.format(sqlStatement, tag.getTagName(), tag.getTagType()));
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
