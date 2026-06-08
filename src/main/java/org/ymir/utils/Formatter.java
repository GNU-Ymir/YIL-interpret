package org.ymir.utils;

import org.ymir.lint.YILNode;

import java.util.HashMap;
import java.util.Map;

public class Formatter {
    private StringBuilder _stream;
    private Map<Long, Long> _yilIds = new HashMap<>();
    private long __UID__;

    public Formatter() {
        this._stream = new StringBuilder();
    }

    public Formatter append(String value) {
        this._stream.append(value);
        return this;
    }

    public Formatter append(long value) {
        this._stream.append(value);
        return this;
    }

    public Formatter append(YILNode node) {
        node.toStream(this);
        return this;
    }

    public long getYilId(long id) {
        Long l = this._yilIds.get(id);
        if (l == null) {
            l = __UID__;
            this._yilIds.put(id, l);
            __UID__++;
        }
        return l;
    }
}
