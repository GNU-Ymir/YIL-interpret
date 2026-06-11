package org.ymir.lint;

import org.ymir.lexing.Word;
import org.ymir.lint.value.YILArrayAccessValue;

import java.util.Objects;

public abstract class YILValue extends YILNode {
    protected final YILType _type;

    protected YILValue(Word loc, YILType type) {
        super(loc);
        this._type = type;
    }

    public YILType getType() {
        return this._type;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILValue val) {
            return this._type.equals(val._type);
        }
        return false;
    }
}
