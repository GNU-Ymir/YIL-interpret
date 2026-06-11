package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;

import java.util.List;

public class YILPtrCallValue extends YILValue {

    private final YILValue _func;
    private final List<YILValue> _params;

    public YILPtrCallValue(Word loc, YILType type, YILValue func, List<YILValue> params) {
        super(loc, type);
        this._func = _func;
        this._params = _params;
    }
}
