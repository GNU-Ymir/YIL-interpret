package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.math.BigInteger;
import java.util.Objects;

public class YILIntValue extends YILValue {

    private final BigInteger _value;

    public YILIntValue(Word loc, YILType type, BigInteger value) {
        super(loc, type);
        this._value = value;
    }

    public BigInteger getValue() {
        return _value;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILIntValue intValue) {
            return this._value.equals(intValue._value);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._value.toString());
    }
}
