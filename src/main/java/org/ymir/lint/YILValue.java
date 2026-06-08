package org.ymir.lint;

import org.ymir.lexing.Word;

public abstract class YILValue extends YILNode {
    protected final YILType _type;

    protected YILValue(Word loc, YILType type) {
        super(loc);
        this._type = type;
    }

    public YILType getType() {
        return this._type;
    }
}
