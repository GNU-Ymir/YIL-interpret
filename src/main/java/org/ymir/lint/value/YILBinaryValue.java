package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;


public class YILBinaryValue extends YILValue {

    private final YILValue _left;
    private final YILValue _right;
    private final BinaryOperator _operator;

    public YILBinaryValue(Word loc, YILType type, YILValue left, YILValue right, BinaryOperator operator) {
        super(loc, type);
        this._left = left;
        this._right = right;
        this._operator = operator;
    }

    public YILValue getLeft() {
        return _left;
    }

    public YILValue getRight() {
        return _right;
    }

    public BinaryOperator getOperator() {
        return _operator;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILBinaryValue binaryValue) {
            return this._operator == binaryValue._operator
                    && this._left.opEquals(binaryValue._left)
                    && this._right.opEquals(binaryValue._right);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._left)
                .append(" ")
                .append(this._operator.content)
                .append(" ")
                .append(this._right);
    }
}
