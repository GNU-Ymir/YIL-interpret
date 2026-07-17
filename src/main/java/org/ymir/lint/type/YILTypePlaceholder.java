package org.ymir.lint.type;

import org.ymir.lint.YILNode;
import org.ymir.lint.YILType;
import org.ymir.utils.Formatter;

public class YILTypePlaceholder extends YILType {

    public YILTypePlaceholder(long uid) {
        super(uid);
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long getAlign() {
        return 0;
    }

    @Override
    public boolean opEquals(YILNode other) {
        return other instanceof YILTypePlaceholder && this._uid == ((YILTypePlaceholder) other).getUid();
    }

    @Override
    public void toStream(Formatter stream) {
        stream.append("<place-holder(").append(this._uid).append(")>");
    }
}
