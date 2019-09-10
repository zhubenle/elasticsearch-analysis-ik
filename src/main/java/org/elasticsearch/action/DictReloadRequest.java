package org.elasticsearch.action;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.single.shard.SingleShardRequest;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <br/>
 * Created on 2019-09-05 11:40.
 *
 * @author zhubenle
 */
public class DictReloadRequest extends SingleShardRequest<DictReloadRequest> {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(DictReloadRequest.class.getName());

    private String path;
    private Map<String, Object> args = new HashMap<>();
    private BytesReference source;

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        path = in.readString();
        args = in.readMap();
        source = in.readBytesReference();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(path);
        out.writeMap(args);
        out.writeBytesReference(source);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public BytesReference getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "DictReloadRequest{"
                + "path='" + path + '\''
                + ", args=" + args
                + "} " + super.toString();
    }
}
