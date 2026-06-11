package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILAddrValue extends YILValue {

    private final YILValue _value;

    public YILAddrValue(Word loc, YILType type, YILValue value) {
        super(loc, type);
        _value = value;
    }

    public YILValue getValue() {
        return _value;
    }

    @Override
    public boolean opEquals(YILNode node) {
        if (Objects.requireNonNull(node) instanceof YILAddrValue addr) {
            if (!super.opEquals(addr)) {
                return false;
            }

        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("&").append(this._value);
    }
}
