package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.lint.type.YILPointer;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILBeginCatchValue extends YILValue {

    private final String _name;

    public YILBeginCatchValue(Word loc, String name) {
        super(loc, YILPointer.YIL_PTR_VOID);
        _name = name;
    }

    public String getName() {
        return _name;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILBeginCatchValue beginCatch) {
            return this._name.equals(beginCatch._name);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {

    }
}
