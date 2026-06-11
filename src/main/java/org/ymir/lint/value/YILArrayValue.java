package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILArrayValue extends YILValue {

    private final List<YILValue> _values;

    public YILArrayValue(Word loc, YILType type, List<YILValue> _values) {
        super(loc, type);
        this._values = _values;
    }

    public List<YILValue> getValues() {
        return _values;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILArrayValue arr) {
            if (this._values.size() != arr._values.size()) return false;
            for (int i = 0; i < this._values.size(); i++) {
                if (!this._values.get(i).opEquals(arr._values.get(i))) return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("[");
        for (int i = 0; i < this._values.size(); i++) {
            if (i > 0) stream.append(", ");
            this._values.get(i).toStream(stream);
        }
        stream.append("]");
    }
}
