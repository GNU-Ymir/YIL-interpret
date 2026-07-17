package org.ymir.lint.global;

import org.ymir.lexing.Word;
import org.ymir.lint.YILGlobal;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.time.Instant;
import java.util.Objects;

public class YILGlobalVar extends YILGlobal {

    private final YILValue _value;
    private final boolean _isThreadLocal;

    public YILGlobalVar(Instant creationTime,
                        String src,
                        Word loc,
                        Word implLoc,
                        String name,
                        boolean isWeak,
                        boolean isThreadLocal,
                        YILValue value) {
        super(creationTime, src, loc, implLoc, name, isWeak);
        this._value = value;
        this._isThreadLocal = isThreadLocal;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILGlobalVar var) {
            return this._name.equals(var._name)
                    && this._value.opEquals(var._value)
                    && this._isWeak == var._isWeak
                    && this._isThreadLocal == var._isThreadLocal;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("var: ");
        if (this._isWeak) {
            stream.append("[weak]");
        }

        stream.append(" ").append(this._name);
        if (this._isThreadLocal) {
            stream.append("@tl");
        }

        stream.append(" ").append(this._value);
    }

    public YILValue getValue() {
        return _value;
    }

    public boolean isThreadLocal() {
        return _isThreadLocal;
    }
}
