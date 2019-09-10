package org.wltea.analyzer.dic;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

public class Monitor implements Runnable {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(Monitor.class.getName());

    private static CloseableHttpClient httpclient = HttpClients.createDefault();
    /*
     * 上次更改时间
     */
    private String last_modified;
    /*
     * 资源属性
     */
    private String eTags;

    /*
     * 请求地址
     */
    private String location;
    private Dictionary dictionary;

    public Monitor(String location, Dictionary dictionary) {
        this.location = location;
        this.dictionary = dictionary;
    }

    @Override
    public void run() {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            this.runUnprivileged();
            return null;
        });
    }

    /**
     * 监控流程：
     * ①向词库服务器发送Head请求
     * ②从响应中获取Last-Modify、ETags字段值，判断是否变化
     * ③如果未变化，休眠1min，返回第①步
     * ④如果有变化，重新加载词典
     * ⑤休眠1min，返回第①步
     */

    public void runUnprivileged() {

        //超时设置
        RequestConfig rc = RequestConfig.custom().setConnectionRequestTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000).build();

        HttpHead head = new HttpHead(location);
        head.setConfig(rc);

        //设置请求头
        if (last_modified != null) {
            head.setHeader("If-Modified-Since", last_modified);
        }
        if (eTags != null) {
            head.setHeader("If-None-Match", eTags);
        }

        CloseableHttpResponse response = null;
        try {

            response = httpclient.execute(head);

            //返回200 才做操作
            if (response.getStatusLine().getStatusCode() == 200) {
                Header lm = response.getLastHeader("Last-Modified");
                Header et = response.getLastHeader("ETag");
                boolean reload = (Objects.nonNull(lm) && !lm.getValue().equalsIgnoreCase(last_modified))
                        || (Objects.nonNull(et) && !et.getValue().equalsIgnoreCase(eTags));
                if (reload) {
                    // 远程词库有更新,需要重新加载词典，并修改last_modified,eTags
                    dictionary.reLoadMainDict();
                    last_modified = Objects.isNull(lm) ? null : lm.getValue();
                    eTags = Objects.isNull(et) ? null : et.getValue();
                }
            } else if (response.getStatusLine().getStatusCode() == 304) {
                //没有修改，不做操作
                //noop
            } else {
                logger.info("remote_ext_dict {} return bad code {}", location, response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            logger.error("remote_ext_dict {} error: {}", location, e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
