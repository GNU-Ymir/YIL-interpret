package org.ymir.lint.value;

import ch.obermuhlner.math.big.BigFloat;
import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILFloatValue extends YILValue {

    private final BigFloat _value;

    public YILFloatValue(Word loc, YILType type, BigFloat value) {
        super(loc, type);
        this._value = value;
    }

    public BigFloat getValue() {
        return _value;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILFloatValue val) {
            return this._value.equals(val._value);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._value.toString());
    }
}
