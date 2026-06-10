package org.ymir.lint.type;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILArray extends YILType {

    private final YILType _inner;
    private final long _len;

    public YILArray(long uid, YILType inner, long len) {
        super(uid);
        _inner = inner;
        _len = len;
    }

    @Override
    public long getSize() {
        return this._inner.getSize() * this._len;
    }

    @Override
    public long getAlign() {
        return this._inner.getAlign();
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILArray array) {
            return array._inner.opEquals(this._inner) && array._len == this._len;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("[")
                .append(this._inner)
                .append(this._len)
                .append("]");
    }

    public long getLen() {
        return _len;
    }

    public YILType getInner() {
        return _inner;
    }
}
