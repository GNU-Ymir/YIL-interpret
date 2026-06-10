package org.ymir.lint.type;

import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILVoid extends YILType {

    public static final YILVoid YIL_VOID = new YILVoid();

    public YILVoid() {}

    @Override
    public long getSize() {
        return 1;
    }

    @Override
    public long getAlign() {
        return 1;
    }

    @Override
    public boolean opEquals(YILNode other) {
        return Objects.requireNonNull(other) instanceof YILVoid;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("void");
    }
}
