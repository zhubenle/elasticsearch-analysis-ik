package org.elasticsearch.action;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestResponseListener;
import org.elasticsearch.rest.action.cat.AbstractCatAction;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.util.Set;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * <br/>
 * Created on 2019-09-04 17:35.
 *
 * @author zhubenle
 */
public class DictReloadCatAction extends AbstractCatAction {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(DictReloadCatAction.class.getName());

    public static final String PATH_CAT_IK_DICT_RELOAD = "/_cat/ik_dict_reload";
    public static final String PATH_CAT_IK_DICT_RELOAD_SINGLE = "/_cat/ik_dict_reload/single";

    public DictReloadCatAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, PATH_CAT_IK_DICT_RELOAD, this);
        controller.registerHandler(GET, PATH_CAT_IK_DICT_RELOAD_SINGLE, this);
    }

    @Override
    protected RestChannelConsumer doCatRequest(RestRequest request, NodeClient client) {
        DictReloadRequest dictReloadRequest = new DictReloadRequest();
        dictReloadRequest.setPath(request.path());
        dictReloadRequest.getArgs().putAll(request.params());

        return restChannel -> {
            client.execute(DictReloadAction.INSTANCE, dictReloadRequest, new RestResponseListener<DictReloadResponse>(restChannel) {

                @Override
                public RestResponse buildResponse(DictReloadResponse dictReloadResponse) throws Exception {
                    logger.info("DictReloadResponse={}", dictReloadResponse);
                    try (XContentBuilder builder = channel.newBuilder()) {
                        builder.map(dictReloadResponse.getMap());
                        return new BytesRestResponse(RestStatus.OK, builder);
                    }
                }
            });
        };
    }

    @Override
    protected void documentation(StringBuilder sb) {
        sb.append("/_cat/ik_dict_reload\n");
    }

    @Override
    protected Table getTableWithHeader(RestRequest request) {
        return null;
    }

    @Override
    protected Set<String> responseParams() {
        return super.responseParams();
    }

    @Override
    public String getName() {
        return "cat_dik_ict_reload_action";
    }
}
