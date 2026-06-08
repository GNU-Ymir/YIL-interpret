package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILAffect extends YILInstr {

    private final YILValue _left;
    private final YILValue _right;

    public YILAffect(Word loc,
                     YILValue left,
                     YILValue right) {
        super(loc);
        _left = left;
        _right = right;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILAffect affect) {
            return this._left.opEquals(affect._left)
                    && this._right.opEquals(affect._right);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._left)
                .append(" = ")
                .append(this._right);
    }

    public YILValue getLeft() {
        return _left;
    }

    public YILValue getRight() {
        return _right;
    }
}
