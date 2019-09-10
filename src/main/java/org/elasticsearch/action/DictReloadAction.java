package org.elasticsearch.action;

import org.elasticsearch.common.io.stream.Writeable;

/**
 * <br/>
 * Created on 2019-09-04 17:35.
 *
 * @author zhubenle
 */
public class DictReloadAction extends ActionType<DictReloadResponse> {

    static final String NAME = "cluster:admin/ik/dict/reload";
    public static final DictReloadAction INSTANCE = new DictReloadAction(NAME, DictReloadResponse::new);

    public DictReloadAction(String name, Writeable.Reader<DictReloadResponse> dictReloadResponseReader) {
        super(name, dictReloadResponseReader);
    }
}
