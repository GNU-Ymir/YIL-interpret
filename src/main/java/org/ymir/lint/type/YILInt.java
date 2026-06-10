package org.ymir.lint.type;

import org.ymir.global.GlobalMachineState;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

import java.util.Objects;

public class YILInt extends YILType {

    public static final YILInt i8 = new YILInt(8, false);
    public static final YILInt i16 = new YILInt(16, false);
    public static final YILInt i32 = new YILInt(32, false);
    public static final YILInt i64 = new YILInt(64, false);
    public static final YILInt isize = new YILInt(0, false);

    public static final YILInt u8 = new YILInt(8, true);
    public static final YILInt u16 = new YILInt(16, true);
    public static final YILInt u32 = new YILInt(32, true);
    public static final YILInt u64 = new YILInt(64, true);
    public static final YILInt usize = new YILInt(0, true);

    private final long _size;
    private final boolean _isSigned;

    public YILInt(long size, boolean isSigned) {
        this._size = size;
        this._isSigned = isSigned;
    }

    @Override
    public long getSize() {
        if (this._size == 0) {
            return GlobalMachineState.POINTER_ARCH_SIZE / 8;
        }
        return this._size / 8;
    }

    @Override
    public long getAlign() {
        if (this._size == 0) {
            return GlobalMachineState.POINTER_ARCH_SIZE / 8;
        }
        return this._size / 8;
    }

    public boolean isSigned() {
        return _isSigned;
    }

    @Override
    public boolean opEquals(YILNode other) {
        if (Objects.requireNonNull(other) instanceof YILInt i) {
            return this._size == i.getSize() && this._isSigned == i._isSigned;
        }
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        if (this._isSigned) {
            stream.append("i");
        } else {
            stream.append("u");
        }

        if (this._size == 0) {
            stream.append("size");
        } else {
            stream.append(this._size);
        }
    }

}
