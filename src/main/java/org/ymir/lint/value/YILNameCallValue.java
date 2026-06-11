package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILNameCallValue extends YILValue {

    private final String _funcName;
    private final List<YILValue> _params;

    public YILNameCallValue(Word loc, YILType type, String funcName, List<YILValue> params) {
        super(loc, type);
        this._params = params;
        this._funcName = funcName;
    }

    public List<YILValue> getParams() {
        return _params;
    }

    public String getFuncName() {
        return _funcName;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILNameCallValue name) {
            if (!this._funcName.equals(name._funcName)) return false;
            if (this._params.size() != name._params.size()) return false;
            for (int i = 0; i < this._params.size(); i++) {
                if (!this._params.get(i).opEquals(name._params.get(i))) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._funcName).append("(");
        for (int i = 0; i < this._params.size(); i++) {
            if (i > 0) stream.append(", ");
            stream.append(this._params.get(i));
        }
        stream.append(")");
    }
}
