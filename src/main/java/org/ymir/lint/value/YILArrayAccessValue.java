package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILArrayAccessValue extends YILValue {

    private final YILValue _array;
    private final YILValue _index;

    public YILArrayAccessValue(Word loc, YILType type, YILValue array, YILValue index) {
        super(loc, type);
        _array = array;
        _index = index;
    }

    public YILValue getArray() {
        return _array;
    }

    public YILValue getIndex() {
        return _index;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILArrayAccessValue acc) {
            return super.opEquals(other)
                    && this._array.opEquals(acc.getArray())
                    && this._index.opEquals(acc.getIndex());
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._array).append ("[").append(this._index).append("]");
    }
}
