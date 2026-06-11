package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

public class YILVarValue extends YILValue {

    private final String _name;
    private final boolean _isTemp;
    private final long _varId;
    private final boolean _isThreadLocal;

    public YILVarValue(Word loc, YILType type, long varId, String name, boolean isTemp, boolean isThreadLocal) {
        super(loc, type);
        this._name = name;
        this._isTemp = isTemp;
        this._varId = varId;
        this._isThreadLocal = isThreadLocal;
    }

    public String getName() {
        return _name;
    }

    public boolean isTemp() {
        return _isTemp;
    }

    public long getVarId() {
        return _varId;
    }

    public boolean isThreadLocal() {
        return _isThreadLocal;
    }

    @Override
    public boolean opEquals(YILNode other) {
        return super.opEquals(other);
    }

    @Override
    public void toStream(Formatter stream) {
        if (this.isTemp() && this._varId != 0) {
            var id = stream.getYilId(this._varId);
            stream.append("YI_").append(id);
        } else {
            stream.append(_name);
            if (this._isThreadLocal) {
                stream.append("@tl");
            }
        }
    }
}
