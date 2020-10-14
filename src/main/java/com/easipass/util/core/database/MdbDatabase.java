package com.easipass.util.core.database;

import com.alibaba.fastjson.JSONObject;
import com.easipass.util.core.Database;
import com.easipass.util.core.exception.ErrorException;
import com.easipass.util.core.exception.SqlException;
import com.easipass.util.core.exception.WarningException;
import com.easipass.util.core.util.StringUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * mdb数据库
 *
 * @author ZJ
 * */
public final class MdbDatabase extends Database {

    /**
     * 构造函数
     *
     * @param path 路径
     */
    public MdbDatabase(String path) throws WarningException {
        super(getConnection(path));
    }

    /**
     * 获取驱动
     *
     * @param path 路径
     *
     * @return Connection
     * */
    private static Connection getConnection(String path) throws WarningException {
        try {
            return DriverManager.getConnection("jdbc:ucanaccess://" + path);
        } catch (SQLException e) {
            throw new WarningException("不是正确的mdb文件");
        }
    }

    /**
     * 获取表数据数量
     *
     * @param tableName 表名
     *
     * @return 数据数量
     * */
    public static int getTableCount(String path, String tableName) throws WarningException {
        MdbDatabase mdbDatabase = new MdbDatabase(path);

        try {
            String count = getFiledData(mdbDatabase.query("SELECT COUNT(*) COUNT FROM " + tableName), "COUNT", true);

            if (StringUtil.isEmpty(count)) {
                return 0;
            }

            return Integer.parseInt(count);
        } catch (SqlException e) {
            throw new WarningException(e.getMessage());
        } finally {
            mdbDatabase.close();
        }
    }

    /**
     * 检查数据是否存在
     *
     * @param sql sql
     *
     * @return sql能查到数据返回true
     * */
    public static boolean dataIsExist(String path, String sql) {
        try {
            MdbDatabase mdbDatabase = new MdbDatabase(path);
            ResultSet resultSet = mdbDatabase.query(sql);
            boolean result = resultSet.next();
            mdbDatabase.close();
            return result;
        } catch (SQLException | WarningException e) {
            throw new ErrorException(e.getMessage());
        }
    }

    /**
     * 获取表所有数据
     *
     * @param path mdb表路径
     * @param tableName 表名
     *
     * @return 数据
     * */
    public static List<JSONObject> getTableData(String path, String tableName) {
        try {
            MdbDatabase mdbDatabase = new MdbDatabase(path);
            List<JSONObject> result = mdbDatabase.queryToJson("SELECT * FROM " + tableName);
            mdbDatabase.close();
            return result;
        } catch (WarningException e) {
            throw new ErrorException(e.getMessage());
        }
    }

}