package org.ymir.lint.instr;

import org.ymir.lexing.Word;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILCall extends YILInstr {

    private final YILValue _content;

    public YILCall(Word loc, YILValue content) {
        super(loc);
        _content = content;
    }

    public YILValue getContent() {
        return _content;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILCall call) {
            return this._content.opEquals(call._content);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append(this._content);
    }
}
