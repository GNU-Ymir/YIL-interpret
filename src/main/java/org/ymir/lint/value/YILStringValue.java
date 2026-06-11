package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.lint.type.YILArray;
import org.ymir.utils.Formatter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class YILStringValue extends YILValue {

    private final byte[] _content;

    public YILStringValue(Word loc, YILType type, byte[] content) {
        super(loc, type);
        this._content = content;
    }

    public byte[] getContent() {
        return _content;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILStringValue val) {
            return Arrays.equals(this._content, val._content);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("\"");
        if (this._type instanceof YILArray array && array.getInner().getSize() == 1) {
            stream.append(new String(this._content, StandardCharsets.UTF_8));
        } else if (this._type instanceof YILArray array && array.getInner().getSize() == 2) {
            stream.append(new String(this._content, StandardCharsets.UTF_16));
        } else if (this._type instanceof YILArray array && array.getInner().getSize() == 4) {
            stream.append(new String(this._content, StandardCharsets.UTF_32));
        }
        stream.append("\"");
    }
}
