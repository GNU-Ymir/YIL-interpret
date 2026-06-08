package org.ymir.lint;

import org.ymir.lexing.Word;

public abstract class YILType extends YILNode {
    protected long _uid;
    private static long __UID__;

    public YILType(Word loc) {
        super(loc);
        this._uid = __UID__;
        __UID__++;
    }

    public long getUid() {
        return this._uid;
    }

    public void setUid(long uid) {
        this._uid = uid;
    }

    public abstract int getSize();
    public abstract int getAlign();
}
