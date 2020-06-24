package com.easipass.epUtil.component;

import com.easipass.epUtil.exception.ErrorException;
import com.easipass.epUtil.util.DateUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    /**
     * 单例
     * */
    private final static Log log = new Log();

    /**
     * 构造函数
     * */
    private Log() {}

    /**
     * 获取单例
     * */
    public static Log getLog() {
        return Log.log;
    }

    /**
     * INFO
     * */
    public void info(String log) {
        this.outputLog("INFO", log);
    }

    /**
     * ERROR
     * */
    public void error(String log) {
        this.outputLog("ERROR", log);
    }

    /**
     * 日志格式
     */
    private String logFormat(String type, String log) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateS = simpleDateFormat.format(date);
        return "[" + dateS + "] - " + "[" + type + "] - " + log + "\n";
    }

    /**
     * 输出日志
     * */
    private void outputLog(String type, String log) {
        // 日志输出文件
        File logFile = new File(System.getProperty("user.dir"), "../log/logFile-" + DateUtil.getDate("yyyy-MM-dd") + ".log");
        // log文件夹
        File logFileParent = logFile.getParentFile();

        // 日志信息
        log = this.logFormat(type, log);

        System.out.print(log);

        // 日志文件夹不存在创建文件
        if (!logFileParent.exists()) {
            if (!logFileParent.mkdirs()) {
                throw ErrorException.getErrorException("创建日志文件夹失败");
            }
        }

        try {
            OutputStream outputStream = new FileOutputStream(logFile, true);
            outputStream.write(log.getBytes());
            outputStream.close();
        } catch (IOException e) {
            throw ErrorException.getErrorException(e.getMessage());
        }
    }

}