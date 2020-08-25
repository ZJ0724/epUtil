package com.easipass.util.core.paramDbComparator;

import com.easipass.util.core.Database;
import com.easipass.util.core.ParamDbComparator;
import com.easipass.util.core.Project;
import com.easipass.util.core.database.MdbDatabase;
import com.easipass.util.core.database.SWGDPARADatabase;
import com.easipass.util.core.exception.ErrorException;
import com.easipass.util.core.exception.WarningException;
import com.easipass.util.core.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 参数库导出比对
 *
 * @author ZJ
 * */
public final class ExportParamDbComparator extends ParamDbComparator {

    /**
     * 日志
     * */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportParamDbComparator.class);

    @Override
    public ComparisonMessage comparison(String groupName, String mdbPath) {
        // 比对信息
        ParamDbComparator.ComparisonMessage result = new ComparisonMessage();

        try {
            // 数据库表名集合
            List<String> dbTables = SWGDPARADatabase.getGroupTables(groupName, "TARGET_TABLE_NAME");
            // mdb表名集合
            List<String> mdbTables = SWGDPARADatabase.getGroupTables(groupName, "SOURCE_TABLE_NAME");

            // 设置比对信息
            result.AllTable = dbTables.size();

            CountDownLatch countDownLatch = new CountDownLatch(mdbTables.size());

            // 遍历数据库表
            for (int i = 0; i < dbTables.size(); i++) {
                final int finalI = i;

                Project.THREAD_POOL_EXECUTOR.execute(() -> {
                    ComparisonMessage.TableFinishMessage finishMessage = new ComparisonMessage.TableFinishMessage();

                    try {
                        String dbTableName = dbTables.get(finalI);
                        String mdbTableName = mdbTables.get(finalI);

                        if (StringUtil.isEmpty(dbTableName)) {
                            throw new WarningException("存在为空的数据库表名");
                        }

                        if (StringUtil.isEmpty(mdbTableName)) {
                            throw new WarningException("存在为空的mdb表名");
                        }

                        finishMessage.dbName = dbTableName;
                        finishMessage.mdbName = mdbTableName;

                        // 数据库表数据数量
                        finishMessage.dbCount = SWGDPARADatabase.getTableCount(dbTableName);

                        // mdb表数据数量
                        finishMessage.mdbCount = MdbDatabase.getTableCount(mdbPath, mdbTableName);

                        // 版本
                        String version = SWGDPARADatabase.getTableVersion(dbTableName);

                        // 数据库表字段
                        List<String> dbTableFields = SWGDPARADatabase.getTableFields(dbTableName, "TARGET_NAME");
                        // mdb表字段
                        List<String> mdbTableFields = SWGDPARADatabase.getTableFields(dbTableName, "SOURCE_NAME");

                        SWGDPARADatabase swgdparaDatabase = new SWGDPARADatabase();
                        try {
                            // 数据库表数据
                            ResultSet resultSet = swgdparaDatabase.query("SELECT * FROM " + SWGDPARADatabase.SWGDPARA + "." + dbTableName + " WHERE 1 = 1 AND PARAMS_VERSION = '" + version + "'");

                            while (resultSet.next()) {
                                // 数据库表单条数据
                                List<String> dataList = new ArrayList<>();

                                for (String field : dbTableFields) {
                                    String fieldValue = Database.getFiledData(resultSet, field);

                                    if (fieldValue == null) {
                                        fieldValue = "";
                                    }

                                    dataList.add(fieldValue.replaceAll("'", "''"));
                                }

                                // 数据库查询sql
                                String sql = "SELECT * FROM " + mdbTableName + " WHERE 1 = 1";

                                for (int j = 0; j < dataList.size(); j ++) {
                                    String data = dataList.get(j);
                                    // mdb字段
                                    String mdbFieldName = mdbTableFields.get(j);

                                    // null
                                    if (StringUtil.isEmptyAll(data)) {
                                        continue;
                                    }

                                    // 字段类型
                                    String fieldType = SWGDPARADatabase.getFieldType(dbTableName, dbTableFields.get(j));

                                    // 兼容日期格式
                                    if ("TIMESTAMP".equals(fieldType)) {
                                        data = parseDate(data);

                                        sql = StringUtil.append(sql, " AND ", mdbFieldName, " = ", "TO_DATE('" + data + "','yyyy-mm-dd hh24:mi:ss')");
                                        continue;
                                    }

                                    // 兼容FLOAT
                                    if ("NUMBER".equals(fieldType)) {
                                        if (data.startsWith(".")) {
                                            data = "0" + data;
                                        }
                                    }

                                    sql = StringUtil.append(sql, " AND ", mdbFieldName, " = '", data, "'");
                                }

                                LOGGER.info(sql);

                                // 查找数据是否存在
                                if (!MdbDatabase.dataIsExist(mdbPath, sql)) {
                                    finishMessage.messages.add(sql);
                                    finishMessage.flag = false;
                                }
                            }
                        } catch (SQLException e) {
                            throw new WarningException(e.getMessage());
                        } finally {
                            swgdparaDatabase.close();
                        }
                    } catch (WarningException e) {
                        finishMessage.flag = false;
                        finishMessage.messages.add(e.getMessage());
                    } finally {
                        if (finishMessage.flag) {
                            finishMessage.messages.add("SUCCESS");
                        }
                        result.currentProgress++;
                        result.finishMessages.add(finishMessage);
                        countDownLatch.countDown();

                        // 发送websocket
                        this.sendData(result.toString());
                    }
                });
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new ErrorException(e.getMessage());
            }
        } catch (WarningException e) {
            result.flag = false;
            result.message = e.getMessage();
        }

        LOGGER.info(result.toString());

        // 发送websocket
        this.sendData(result.toString());

        return result;
    }

}