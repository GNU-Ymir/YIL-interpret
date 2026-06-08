package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILReturn extends YILInstr {
    private final YILValue _value;

    public YILReturn(Word loc,
                     YILValue value) {
        super(loc);
        _value = value;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILReturn ret) {
            return this._value.opEquals(ret._value);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("return ")
                .append (this._value);
    }

    public YILValue getValue() {
        return _value;
    }
}
