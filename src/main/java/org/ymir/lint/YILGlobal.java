package org.ymir.lint;

import org.ymir.lexing.Word;

public abstract class YILGlobal extends YILNode {
    protected final Word _implLoc;
    protected final String _name;
    protected final boolean _isWeak;

    public YILGlobal(Word loc, Word implLoc, String name, boolean isWeak) {
        super(loc);
        this._implLoc = implLoc;
        this._name = name;
        this._isWeak = isWeak;
    }

    public boolean isWeak() {
        return _isWeak;
    }

    public Word getImplLoc() {
        return _implLoc;
    }

    public String getName() {
        return _name;
    }
}
