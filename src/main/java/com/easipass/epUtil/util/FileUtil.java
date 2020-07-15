package com.easipass.epUtil.util;

import com.easipass.epUtil.exception.ErrorException;
import java.io.*;

/**
 * 文件工具
 *
 * @author ZJ
 * */
public final class FileUtil {

    /**
     * 写入文件数据
     *
     * @param file 文件
     * @param data 数据
     * */
    public static void setData(File file, String data) {
        try {
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (IOException e) {
            throw ErrorException.getErrorException(e.getMessage());
        }
    }

    /**
     * 检查生成文件
     *
     * @param file 要生成的文件
     * @param inputStream 文件流
     * */
    public static void createFile(File file, InputStream inputStream) {
        if (file == null) {
            return;
        }

        if (!file.exists()) {
            File par = file.getParentFile();

            if (!par.exists()) {
                boolean is = par.mkdirs();
                if (!is) {
                    throw new ErrorException("创建文件夹失败");
                }
            }

            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                OutputStream outputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                byte[] bytes = new byte[bufferedInputStream.available()];
                bufferedInputStream.read(bytes);
                bufferedOutputStream.write(bytes);

                bufferedInputStream.close();
                outputStream.close();
                bufferedOutputStream.close();
            } catch (IOException e) {
                throw new ErrorException(e.getMessage());
            }
        }
    }

}