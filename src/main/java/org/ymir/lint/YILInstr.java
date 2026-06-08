package org.ymir.lint;

import org.ymir.lexing.Word;

public abstract class YILInstr extends YILNode {
    public YILInstr(Word loc) {
        super(loc);
    }
}
