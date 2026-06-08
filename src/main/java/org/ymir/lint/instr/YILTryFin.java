package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILTryFin extends YILInstr {
    private final List<YILInstr> _tryPart;
    private final List<YILInstr> _finPart;


    public YILTryFin(Word loc, List<YILInstr> tryPart, List<YILInstr> finPart) {
        super(loc);
        _tryPart = tryPart;
        _finPart = finPart;
    }

    public List<YILInstr> getTryPart() {
        return _tryPart;
    }

    public List<YILInstr> getFinPart() {
        return _finPart;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILTryFin tr) {
            if (tr._finPart.size () != this._finPart.size()) {
                return false;
            }
            if (tr._tryPart.size () != this._tryPart.size ()) {
                return false;
            }

            for (int i = 0 ; i < this._finPart.size () ; i++) {
                if (!this._finPart.get(i).opEquals(tr._finPart.get(i))) {
                    return false;
                }
            }

            for (int i = 0 ; i < this._tryPart.size () ; i++) {
                if (!this._tryPart.get(i).opEquals(tr._tryPart.get(i))) {
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("try {\n");
        this._tryPart.forEach(item-> stream
                .append(item)
                .append(";\n"));
        stream.append("}\nfinally{\n");
        this._finPart.forEach(item-> stream
                .append(item)
                .append(";\n"));
        stream.append("}");
    }
}
