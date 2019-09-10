package org.elasticsearch.action;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.single.shard.TransportSingleShardAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportException;
import org.elasticsearch.transport.TransportRequestOptions;
import org.elasticsearch.transport.TransportResponseHandler;
import org.elasticsearch.transport.TransportService;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <br/>
 * Created on 2019-09-05 11:43.
 *
 * @author zhubenle
 */
public class TransportDictReloadAction extends TransportSingleShardAction<DictReloadRequest, DictReloadResponse> {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(TransportDictReloadAction.class.getName());

    private final String nodeName;

    @Inject
    public TransportDictReloadAction(ThreadPool threadPool, ClusterService clusterService,
                                     TransportService transportService, ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver) {
        super(DictReloadAction.NAME, threadPool, clusterService, transportService, actionFilters,
                indexNameExpressionResolver,
                in -> new DictReloadRequest(),
                ThreadPool.Names.SAME);
        this.nodeName = clusterService.getNodeName();
    }

    @Override
    protected DictReloadResponse shardOperation(DictReloadRequest request, ShardId shardId) throws IOException {
        switch (request.getPath()) {
            case DictReloadCatAction.PATH_CAT_IK_DICT_RELOAD:
                return reloadDict(request);
            case DictReloadCatAction.PATH_CAT_IK_DICT_RELOAD_SINGLE:
                logger.info("nodeName={}, 重新加载index={}字典", nodeName, request.getArgs().get("index"));
                DictReloadResponse response = new DictReloadResponse();
                response.getMap().put(nodeName, "success");
                System.gc();
                return response;
            default:
                DictReloadResponse response2 = new DictReloadResponse();
                response2.getMap().put(nodeName, request.getPath() + " not found");
                return response2;
        }

    }

    @Override
    protected Writeable.Reader<DictReloadResponse> getResponseReader() {
        return in -> {
            DictReloadResponse response = new DictReloadResponse();
            response.readFrom(in);
            return response;
        };
    }

    @Override
    protected boolean resolveIndex(DictReloadRequest request) {
        return false;
    }

    @Override
    protected ShardsIterator shards(ClusterState state, InternalRequest request) {
        return null;
    }

    private DictReloadResponse reloadDict(DictReloadRequest req) {
        DictReloadRequest request = new DictReloadRequest();
        request.setPath(DictReloadCatAction.PATH_CAT_IK_DICT_RELOAD_SINGLE);
        request.getArgs().putAll(req.getArgs());

        ClusterState clusterState = clusterService.state();
        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);
        DiscoveryNodes nodes = clusterState.nodes();

        final CountDownLatch countDownLatch = new CountDownLatch(nodes.getSize());
        final Map<String, String> result = new HashMap<>();
        for (final DiscoveryNode node : nodes) {
            result.put(node.getAddress().toString(), "time out");
            TransportResponseHandler<DictReloadResponse> responseHandler = new TransportResponseHandler<DictReloadResponse>() {
                @Override
                public DictReloadResponse read(StreamInput in) throws IOException {
                    DictReloadResponse instance = new DictReloadResponse();
                    instance.readFrom(in);
                    return instance;
                }

                @Override
                public void handleResponse(DictReloadResponse response) {
                    result.put(node.getAddress().toString(), response.getMap().toString());
                    countDownLatch.countDown();
                }

                @Override
                public void handleException(TransportException exp) {
                    logger.error("刷新节点{}字典异常: ", node.getName(), exp);
                    result.put(node.getAddress().toString(), "err :" + exp.getMessage());
                    countDownLatch.countDown();
                }

                @Override
                public String executor() {
                    return ThreadPool.Names.SAME;
                }
            };

            transportService.sendRequest(node, DictReloadAction.NAME, request, TransportRequestOptions.builder().withTimeout(10000).build(),
                    responseHandler);
        }

        try {
            countDownLatch.await(20, TimeUnit.SECONDS);
        } catch (Exception e) {
        }

        DictReloadResponse response = new DictReloadResponse();
        response.getMap().putAll(result);
        return response;
    }
}
