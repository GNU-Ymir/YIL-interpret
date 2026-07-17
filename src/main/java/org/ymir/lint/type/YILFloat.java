package org.ymir.lint.type;

import org.ymir.global.GlobalMachineState;
import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

public class YILFloat extends YILType {

    public static final YILFloat f32 = new YILFloat(32);
    public static final YILFloat f64 = new YILFloat(64);
    public static final YILFloat f80 = new YILFloat(80);
    public static final YILFloat fsize = new YILFloat(0);

    private final long _size;

    public YILFloat(long _size) {
        this._size = _size;
    }


    @Override
    public long getSize() {
        if (this._size == 0) {
            return GlobalMachineState.FLOAT_ARCH_SIZE / 8;
        }

        return this._size / 8;
    }

    @Override
    public long getAlign() {
        if (this._size == 0) {
            return GlobalMachineState.FLOAT_ARCH_SIZE / 8;
        }

        return this._size / 8;
    }

    @Override
    public boolean opEquals(YILNode other) {
        return false;
    }

    @Override
    public void toStream(Formatter stream) {
        if (this._size == 0) {
            stream.append("fsize");
        } else {
            stream.append("f" + this._size);
        }
    }
}
