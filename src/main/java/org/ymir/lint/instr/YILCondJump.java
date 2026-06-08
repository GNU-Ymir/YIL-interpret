package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILCondJump extends YILInstr {

    private final YILValue _cond;
    private final YILLabel _then;
    private final YILLabel _else;

    public YILCondJump(Word loc,
                       YILValue _cond,
                       YILLabel _then,
                       YILLabel _else) {
        super(loc);
        this._cond = _cond;
        this._then = _then;
        this._else = _else;
    }

    public YILValue getCond() {
        return _cond;
    }

    public YILLabel getThen() {
        return _then;
    }

    public YILLabel getElse() {
        return _else;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILCondJump jump) {
            return this._cond.opEquals(jump._cond)
                    && this._then.opEquals(jump._then)
                    && this._else.opEquals(jump._else);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("#IF ").append(this._cond).append("\n");
        stream.append("#THEN_GOTO ")
                .append(this._then.getName())
                .append("#")
                .append(this._then.getLabelId());
        stream.append("#ELSE_GOTO ")
                .append(this._else.getName())
                .append("#")
                .append(this._else.getLabelId());
    }
}
