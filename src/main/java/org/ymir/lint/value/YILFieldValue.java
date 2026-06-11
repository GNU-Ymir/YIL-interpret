package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILFieldValue extends YILValue {

    private final YILValue _tuple;
    private final long _index;
    private final String _name;

    public YILFieldValue(Word loc, YILType type, YILValue tuple, long index, String name) {
        super(loc, type);
        this._tuple = tuple;
        this._index = index;
        this._name = name;
    }

    public YILValue getTuple() {
        return _tuple;
    }

    public long getIndex() {
        return _index;
    }

    public String getName() {
        return _name;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILFieldValue fieldValue) {
            return this._tuple.opEquals(fieldValue._tuple)
                    && this._index == fieldValue._index
                    && this._name.equals(fieldValue._name);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        if (this._tuple instanceof YILUnrefValue unref) {
            stream.append(unref.getValue()).append("-> ");
        } else {
            stream.append(this._tuple).append(".");
        }

        if (this._name.length() > 0) {
            stream.append(this._name);
        } else {
            stream.append(this._index);
        }
    }
}
