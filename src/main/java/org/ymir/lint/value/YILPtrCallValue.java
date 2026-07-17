package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILPtrCallValue extends YILValue {

    private final YILValue _func;
    private final List<YILValue> _params;

    public YILPtrCallValue(Word loc, YILType type, YILValue func, List<YILValue> params) {
        super(loc, type);
        this._func = func;
        this._params = params;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILPtrCallValue ot) {
            if (!ot._func.opEquals(_func)) {
                return false;
            }
            for (int i = 0; i < _params.size(); i++) {
                if (!ot._params.get(i).opEquals(_params.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("(*").append(this._func).append(")(");
        for (int i = 0; i < this._params.size(); i++) {
            if (i > 0) stream.append(", ");
            stream.append(this._params.get(i));
        }
        stream.append(")");
    }
}
