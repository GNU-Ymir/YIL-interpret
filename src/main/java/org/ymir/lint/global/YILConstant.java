package org.ymir.lint.global;

import org.ymir.lexing.Word;
import org.ymir.lint.YILGlobal;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.time.Instant;
import java.util.Objects;

public class YILConstant extends YILGlobal {

    private final YILValue _value;
    private boolean _isLocal;

    public YILConstant(Instant creationTime,
                       String src,
                       Word loc,
                       Word implLoc,
                       String name,
                       boolean isWeak,
                       boolean isLocal,
                       YILValue value) {
        super(creationTime, src, loc, implLoc, name, isWeak);
        this._value = value;
        this._isLocal = isLocal;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILConstant constant) {
            return this._name.equals(constant._name)
                    && this._isWeak == (constant._isWeak)
                    && this._value.opEquals(constant._value);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("const: ");
        if (this._isLocal || this._isWeak) {
            stream.append("[");
            if (this._isWeak) {
                stream.append("weak");
                if (this._isLocal) stream.append(",");
            }
            if (this._isLocal) {
                stream.append("local");
            }
            stream.append("]");
        }
        stream.append(" ").append(this._name).append(" ");
        this._value.toStream(stream);
    }

    public YILValue getValue() {
        return this._value;
    }

    public boolean is_isLocal() {
        return _isLocal;
    }

    public void set_isLocal(boolean _isLocal) {
        this._isLocal = _isLocal;
    }
}
