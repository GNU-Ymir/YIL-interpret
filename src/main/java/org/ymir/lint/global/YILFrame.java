package org.ymir.lint.global;

import org.ymir.lexing.Word;
import org.ymir.lint.YILGlobal;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.instr.YILVarDecl;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILFrame extends YILGlobal {

    private List<YILVarDecl> _params;
    private YILType _retType;
    private YILInstr _body;
    private boolean _isGlobalCtor;

    public YILFrame(Word loc,
                    Word implLoc,
                    String name,
                    boolean isWeak,
                    List<YILVarDecl> params,
                    YILType retType,
                    YILInstr body,
                    boolean isGlobalCtor) {
        super(loc, implLoc, name, isWeak);
        this._params = params;
        this._retType = retType;
        this._body = body;
        this._isGlobalCtor = isGlobalCtor;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILFrame frame) {
            if (this._params.size() != frame._params.size()) {
                return false;
            }
            for (int i = 0; i < this._params.size(); i++) {
                if (!this._params.get(i).opEquals(frame._params.get(i))) {
                    return false;
                }
            }
            return this._name.equals(frame._name)
                    && this._isWeak == (frame._isWeak)
                    && this._retType.opEquals(frame._retType)
                    && this._body.opEquals(frame._body);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("frame: ");
        if (this._isGlobalCtor) {
            stream.append(" [package]");
        }
        if (this._isWeak) {
            stream.append(" [weak]");
        }
        stream.append(" ").append(this._name).append(" ");
        stream.append("(");
        for (int i = 0; i < this._params.size(); i++) {
            if (i != 0) stream.append(", ");
            stream.append(this._params.get(i));
        }

        stream.append(")-> ").append(this._retType);
        stream.append(" ");
        stream.append(this._body);
    }

    public boolean isGlobalCtor() {
        return _isGlobalCtor;
    }

    public YILInstr getBody() {
        return _body;
    }

    public YILType getRetType() {
        return _retType;
    }

    public List<YILVarDecl> getParams() {
        return _params;
    }
}
