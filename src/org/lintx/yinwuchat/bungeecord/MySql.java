/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lintx.yinwuchat.bungeecord;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jjcbw01
 */
public class MySql {
    private static String db_url;
    private static String username;
    private static String password;
    
    private static LinkedList<Connection> linkedlist = new LinkedList<Connection>();
    /**
    * 最小连接数量
    */
    private static int jdbcConnectionInitSize = 10;

    /**
    * 当前最大连接数量=max*jdbcConnectionInitSize
    */
    private static int max = 1;
    
    public MySql(String host,int port,String database,String username,String password){
        MySql.db_url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
        MySql.username = username;
        MySql.password = password;
    }
    
    public Connection getConnection() throws SQLException {
        /**
         * 如果集合中没有数据库连接对象了，且创建的数据库连接对象没有达到最大连接数量，可以再创建一组数据库连接对象以备使用
         * 
         */
        if (linkedlist.size() == 0 && max <= 5) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < jdbcConnectionInitSize; i++) {
                Connection conn = DriverManager.getConnection(db_url, username, password);
                /**
                 * 将创建好的数据库连接对象添加到Linkedlist集合中
                 * 
                 */
                linkedlist.add(conn);
            }
            max++;
        }
        if (linkedlist.size() > 0) {
            /**
             * 从linkedlist集合中取出一个数据库链接对象Connection使用
             * 
             */
            final Connection conn1 = linkedlist.removeFirst();
            /**
             * 返回一个Connection对象，并且设置Connection对象方法调用的限制， 当调用connection类对象的close()方法时会将Connection对象重新收集放入linkedlist集合中。
             */
            return (Connection) Proxy.newProxyInstance(
                /**
                 * 这里换成JdbcConnectionsPool.class.getClassLoader();也可以
                 */
                conn1.getClass().getClassLoader(),
                conn1.getClass().getInterfaces(), new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,Object[] args) throws Throwable {
                        if (!method.getName().equalsIgnoreCase("close")) {
                            return method.invoke(conn1, args);
                        } else {
                            linkedlist.add(conn1);
                            return null;
                        }
                    }
                }
            );
        }
        return null;
    }

    public void release(Connection conn, Statement st, ResultSet rs) {
        if (rs != null) {
            try {
                /**
                 * 关闭存储查询结果的ResultSet对象
                 * 
                 */
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rs = null;
        }
        if (st != null) {
            try {
                /**
                 * 关闭负责执行SQL命令的Statement对象
                 * 
                 */
                st.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                /**
                 * 关闭Connection数据库连接对象
                 * 
                 */
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
    * 执行sql语句，增删改
    *
    * @param sql
    * @param params
    * @return
    */
    public boolean execute(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean res = true;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            ps.execute();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            res = false;
            return res;
        } finally {
            release(conn, ps, null);
        }
    }
    
    public int insert(String sql, Object... params){
        Connection conn = null;
        PreparedStatement ps = null;
        int last_id = -1;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return last_id;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    last_id = (int)generatedKeys.getLong(1);
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release(conn, ps, null);
        }
        return last_id;
    }

    /**
    * 查询数据
    *
    * @param sql
    * @param params
    * @return
    */
    public List<Map<String, Object>> query(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            rs = ps.executeQuery();
            /**
             * 获得结果集结构信息,元数据
             */
            ResultSetMetaData md = rs.getMetaData();
            /**
             * 获得列数
             */
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnLabel(i), rs.getObject(i));
                }
                list.add(rowData);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            release(conn, ps, rs);
        }
    }
}
