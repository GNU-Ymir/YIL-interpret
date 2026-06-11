package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILUnaryValue extends YILValue {

    private final YILValue _value;
    private final UnaryOperator _operator;

    public YILUnaryValue(Word loc, YILType type, UnaryOperator operator, YILValue value) {
        super(loc, type);
        this._value = value;
        this._operator = operator;
    }

    public YILValue getValue() {
        return _value;
    }

    public UnaryOperator getOperator() {
        return _operator;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILUnaryValue unaryValue) {
            return this._value.opEquals(unaryValue.getValue())
                    && this._operator.equals(unaryValue.getOperator());
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._operator.content).append(this._value);
    }
}
