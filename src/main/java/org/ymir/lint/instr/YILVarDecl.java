package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILVarDecl extends YILInstr {
    private final String _name;
    private final long _varId;
    private final YILType _type;
    private final boolean _isTemp;

    public YILVarDecl(Word loc,
                      String name,
                      long varId,
                      YILType type,
                      boolean isTemp) {
        super(loc);
        this._name = name;
        this._varId = varId;
        this._type = type;
        this._isTemp = isTemp;
    }

    public String getName() {
        return _name;
    }

    public long getVarId() {
        return _varId;
    }

    public YILType getType() {
        return _type;
    }

    public boolean isTemp() {
        return this._isTemp;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILVarDecl var) {
            return this._varId == var._varId;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        if (this._isTemp && this._varId != 0) {
            var id = stream.getYilId(this._varId);
            stream.append("let YI_").append(id);
            stream.append(" : ").append(this._type);
        } else {
            stream.append("let ").append(this._name);
            stream.append(" : ").append(this._type);
        }
    }
}
