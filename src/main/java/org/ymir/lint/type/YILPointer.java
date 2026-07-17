package org.ymir.lint.type;

import org.ymir.global.GlobalMachineState;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILPointer extends YILType {

    public static final YILPointer YIL_PTR_VOID = new YILPointer(1, YILVoid.YIL_VOID);

    private YILType _inner;

    public YILPointer(long uid, YILType inner) {
        super(uid);
        _inner = inner;
    }


    @Override
    public long getSize() {
        return GlobalMachineState.POINTER_ARCH_SIZE / 8;
    }

    @Override
    public long getAlign() {
        return GlobalMachineState.POINTER_ARCH_SIZE / 8;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILPointer pointer) {
            return this._inner.opEquals(pointer._inner);
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("*").append(this._inner);
    }

    public YILType getInner() {
        return _inner;
    }

    public void setInner(YILType inner) {
        this._inner = inner;
    }
}
