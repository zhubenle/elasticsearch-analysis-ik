package org.elasticsearch.action;

import org.elasticsearch.common.io.stream.Writeable;

/**
 * <br/>
 * Created on 2019-09-04 17:35.
 *
 * @author zhubenle
 */
public class DictReloadAction extends ActionType<DictReloadResponse> {

    public static final DictReloadAction INSTANCE = new DictReloadAction();
    static final String NAME = "cluster:admin/ik/dict/reload";

    private DictReloadAction() {
        super(NAME);
    }

    @Override
    public Writeable.Reader<DictReloadResponse> getResponseReader() {
        return in -> {
            DictReloadResponse response = new DictReloadResponse();
            response.readFrom(in);
            return response;
        };
    }
}
