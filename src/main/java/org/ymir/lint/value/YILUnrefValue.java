package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILUnrefValue extends YILValue {

    private final YILValue _value;

    public YILUnrefValue(Word loc, YILType type, YILValue value) {
        super(loc, type);
        this._value = value;
    }

    public YILValue getValue() {
        return _value;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILUnrefValue unref) {
            return this._value.equals(unref._value);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("*").append(this._value);
    }
}
