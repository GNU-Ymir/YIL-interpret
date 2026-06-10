package org.ymir.lint.type;

import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class YILTuple extends YILType {

    private static HashSet<YILTuple> __TUPLE_CHECKER__ = new HashSet<YILTuple>();

    private List<YILType> _inners;
    private List<String> _fieldNames;
    private final boolean _isUnion;
    private final boolean _isPacked;

    private final boolean _isPrepared = false;

    public YILTuple(long uid,
                    List<YILType> inners,
                    List<String> fieldNames,
                    boolean isUnion,
                    boolean isPacked) {
        super(uid);
        _inners = inners;
        _fieldNames = fieldNames;
        _isUnion = isUnion;
        _isPacked = isPacked;
    }

    public YILTuple() {
        super(0);
        _inners = new ArrayList<>();
        _fieldNames = new ArrayList<>();
        _isUnion = false;
        _isPacked = false;
    }

    @Override
    public long getSize() {
        if (this._isUnion) {
            return this.getUnionSize();
        }
        if (this._isPacked) {
            return this.getPackedSize();
        }

        var current = 0L;
        var glob = 0L;
        var globAlign = 0L;
        for  (YILType inner : _inners) {
            var innerSize = inner.getSize();
            var align = inner.getAlign();

            if (align > globAlign) {
                globAlign = align;
            }

            var padding = (align - (current % align)) % align;
            var aligned = (current + padding);

            current = aligned + innerSize;
            glob += padding + innerSize;
        }

        if (glob % globAlign != 0) {
            glob = glob + (globAlign - (glob % globAlign));
        }

        return glob;
    }

    @Override
    public long getAlign() {
        if (this._isPacked) {
            return 1L;
        }

        var max = 1L;
        for (YILType inner : _inners) {
            var align = inner.getAlign();
            if (align > max) {
                max = align;
            }
        }

        return max;
    }

    private long getUnionSize() {
        var max = 1L;
        for (YILType inner : _inners) {
            var size =  inner.getSize();
            if(size > max) {
                max = size;
            }
        }

        return max;
    }

    private long getPackedSize() {
        var all = 0L;
        for (YILType inner : _inners) {
            all += inner.getSize();
        }

        if (all == 0L) {
            return 1;
        }

        return all;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILTuple tu) {
            if (this == other) {
                return true;
            }
            if (this._isPrepared) {
                return false;
            }

            if (this._isPacked != tu._isPacked) {
                return false;
            }

            if (this._isUnion != tu._isUnion) {
                return false;
            }

            var sIn = YILTuple.__TUPLE_CHECKER__.contains(this);
            var oIn = YILTuple.__TUPLE_CHECKER__.contains(tu);

            if (sIn && oIn) {
                return true;
            }

            __TUPLE_CHECKER__.add(this);
            __TUPLE_CHECKER__.add(tu);
            try {
                if (this._fieldNames.size() != tu._fieldNames.size()) {
                    return false;
                }

                if (this._inners.size() != tu._inners.size()) {
                    return false;
                }

                for  (int i = 0; i < this._inners.size(); i++) {
                    if (!this._inners.get(i).opEquals(tu._inners.get(i))) {
                        return false;
                    }
                }

                for (int i = 0; i < this._fieldNames.size(); i++) {
                    if (!this._fieldNames.get(i).equals(tu._fieldNames.get(i))) {
                        return false;
                    }
                }
            } finally {
                __TUPLE_CHECKER__.remove(this);
                __TUPLE_CHECKER__.remove(tu);
            }

            return true;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("(");
        var i = 0;
        for (YILType inner : _inners) {
            if (i > 0) {
                stream.append(", ");
            }
            inner.toStream(stream);
            i++;
        }
        stream.append(")");
    }

    public List<YILType> getInners() {
        return _inners;
    }

    public void setInners(List<YILType> inners, List<String> fieldNames) {
        this._inners = inners;
        this._fieldNames = fieldNames;
    }

    public List<String> getFieldNames() {
        return _fieldNames;
    }

    public boolean isUnion() {
        return _isUnion;
    }

    public boolean isPacked() {
        return _isPacked;
    }

    @Override
    public int hashCode() {
        if (this._isPrepared) {
            return (int) this._uid;
        }
        else {
            return super.hashCode();
        }
    }
}
