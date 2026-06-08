package org.ymir.lint;

import org.ymir.lexing.Word;
import org.ymir.utils.Formatter;

public abstract class YILNode {
    protected final Word _loc;

    public YILNode(Word loc) {
        this._loc = loc;
    }

    public Word getLoc() {
        return this._loc;
    }

    public abstract boolean opEquals(YILNode other);
    public abstract void toStream(Formatter stream);
}
