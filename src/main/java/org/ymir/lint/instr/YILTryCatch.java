package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILTryCatch extends YILInstr {
    private final YILType _catchType;

    private final List<YILInstr> _tryPart;
    private final List<YILInstr> _catchPart;

    public YILTryCatch(Word loc,
                       YILType catchType,
                       List<YILInstr> tryPart,
                       List<YILInstr> catchPart) {
        super(loc);
        _catchType = catchType;
        _tryPart = tryPart;
        _catchPart = catchPart;
    }

    public YILType getCatchType() {
        return _catchType;
    }

    public List<YILInstr> getTryPart() {
        return _tryPart;
    }

    public List<YILInstr> getCatchPart() {
        return _catchPart;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILTryCatch tr) {
            if (tr._catchPart.size () != this._catchPart.size()) {
                return false;
            }
            if (tr._tryPart.size () != this._tryPart.size ()) {
                return false;
            }
            if (!this._catchType.opEquals(tr._catchType)) {
                return false;
            }

            for (int i = 0 ; i < this._catchPart.size () ; i++) {
                if (!this._catchPart.get(i).opEquals(tr._catchPart.get(i))) {
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
        stream.append("}\ncatch{\n");
        this._catchPart.forEach(item-> stream
                .append(item)
                .append(";\n"));
        stream.append("}");
    }
}
