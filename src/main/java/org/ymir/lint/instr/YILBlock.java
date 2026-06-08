package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILBlock extends YILInstr {
    private final List<YILInstr> _instrs;

    public YILBlock(Word loc,
                    List<YILInstr> instrs) {
        super(loc);
        this._instrs = instrs;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILBlock block) {
            if (this._instrs.size() != block._instrs.size()) {
                return false;
            }

            for (int i = 0; i < this._instrs.size(); i++) {
                if (!this._instrs.get(i).opEquals(block._instrs.get(i))) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("{\n");
        this._instrs.forEach(value -> stream
                .append(value)
                .append(";\n"));
        stream.append("}\n");
    }
}
