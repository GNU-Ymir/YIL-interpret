package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILGoto extends YILInstr {
    private final YILLabel _lbl;

    public YILGoto(Word loc, YILLabel lbl) {
        super(loc);
        _lbl = lbl;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILGoto gt) {
            return this._lbl.opEquals(gt._lbl);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("#GOTO ")
                .append(this._lbl.getName())
                .append("#")
                .append(this._lbl.getLabelId());
    }

    public YILLabel getLabel() {
        return _lbl;
    }
}
