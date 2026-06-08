package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILLabel extends YILInstr {
    private final String _name;
    private final long _lblId;

    public YILLabel(Word loc,
                    String name,
                    long lblId) {
        super(loc);
        _name = name;
        _lblId = lblId;
    }

    public String getName() {
        return _name;
    }

    public long getLabelId() {
        return _lblId;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILLabel lbl) {
            return this._lblId == lbl._lblId;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("#LABEL ")
                .append(this._name)
                .append("#")
                .append(this._lblId);

    }
}
