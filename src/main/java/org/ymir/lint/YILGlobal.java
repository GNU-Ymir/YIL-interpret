package org.ymir.lint;

import org.ymir.lexing.Word;

import java.time.Instant;

public abstract class YILGlobal extends YILNode {
    protected final Instant _creationTime;
    protected final String _src;

    protected final Word _implLoc;
    protected final String _name;
    protected final boolean _isWeak;

    public YILGlobal(Instant creationTime, String src, Word loc, Word implLoc, String name, boolean isWeak) {
        super(loc);
        this._creationTime = creationTime;
        this._src = src;
        this._implLoc = implLoc;
        this._name = name;
        this._isWeak = isWeak;
    }

    public Instant getCreationTime() {
        return _creationTime;
    }

    public String getSrc() {
        return _src;
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
