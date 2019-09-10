package org.elasticsearch.action;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <br/>
 * Created on 2019-09-05 11:43.
 *
 * @author zhubenle
 */
public class DictReloadResponse extends ActionResponse implements ToXContentObject {


    private Map<String, Object> map;

    public DictReloadResponse() {
        this.map = new HashMap<>();
    }

    public DictReloadResponse(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        map = in.readMap();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeMap(map);
    }

    @Override
    public String toString() {
        return "DictReloadResponse{"
                + "map=" + map
                + "}";
    }
}
