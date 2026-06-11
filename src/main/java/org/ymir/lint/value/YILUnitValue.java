package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

public class YILUnitValue extends YILValue {
    public YILUnitValue(Word loc, YILType type) {
        super(loc, type);
    }

    @Override
    public boolean opEquals(YILNode other) {
        return other instanceof YILUnitValue;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("<unit>");
    }
}
