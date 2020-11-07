package com.easipass.util.api.websocket;

import com.easipass.util.core.ParamDbComparator;
import com.easipass.util.core.config.Project;
import com.easipass.util.core.exception.ErrorException;
import com.easipass.util.core.paramDbComparator.ExcelImportParamDbComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;

/**
 * 参数库excel导入websocket
 *
 * @author ZJ
 * */
@Component
@ServerEndpoint(BaseWebsocketApi.URL + "excelImportParamDbComparison/{tableName}/{fileName}")
public class ExcelImportParamDbComparisonWebsocketApi extends BaseWebsocketApi {

    /**
     * ParamDbComparator
     * */
    private ParamDbComparator paramDbComparator;

    /**
     * file
     * */
    private File file;

    /**
     * log
     * */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelImportParamDbComparisonWebsocketApi.class);

    /**
     * 建立连接
     *
     * @param session session
     * @param tableName 表名
     * @param fileName 文件名
     * */
    @OnOpen
    public void onOpen(Session session, @PathParam("tableName") String tableName, @PathParam("fileName") String fileName) {
        super.onOpen(session);

        try {
            this.file = new File(Project.CACHE_PATH, fileName);
            this.paramDbComparator = new ExcelImportParamDbComparator(tableName, this.file.getAbsolutePath());

            this.paramDbComparator.addWebsocket(this);
            this.paramDbComparator.comparison();
        } finally {
            this.close();
        }
    }

    /**
     * 监听连接关闭
     * */
    @OnClose
    public void onClose() {
        LOGGER.info("ExcelImportParamDbComparisonWebsocketApi已关闭");
        this.paramDbComparator.deleteWebsocket();
        if (!this.file.delete()) {
            throw new ErrorException("excel文件删除失败");
        }
    }

}