package org.ymir.lint.value;

import org.ymir.lexing.Word;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.utils.Formatter;

import java.util.List;
import java.util.Objects;

public class YILTupleValue extends YILValue {

    private final List<YILValue> _values;
    private final List<String> _fieldNames;

    public YILTupleValue(Word loc, YILType type, List<YILValue> values, List<String> fieldNames) {
        super(loc, type);
        this._values = values;
        this._fieldNames = fieldNames;
    }

    public List<YILValue> getValues() {
        return _values;
    }

    public List<String> getFieldNames() {
        return _fieldNames;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (!super.opEquals(other)) return false;
        if (Objects.requireNonNull(other) instanceof YILTupleValue val) {
            if (this._values.size() != val._values.size()) return false;
            for (int i = 0; i < this._values.size(); ++i) {
                if (!this._values.get(i).opEquals(val._values.get(i))) return false;
            }
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("(");
        for (int i = 0; i < _values.size(); i++) {
            if (i > 0) stream.append(", ");
            stream.append(_values.get(i));
        }
        stream.append(")");
    }
}
