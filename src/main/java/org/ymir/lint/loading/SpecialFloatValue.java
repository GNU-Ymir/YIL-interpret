package org.ymir.lint.loading;

public enum SpecialFloatValue {
    NAN(1),
    INFP(2),
    INFN(3),
    HEX(4);

    public final int value;

    SpecialFloatValue(int value) {
        this.value = value;
    }
}
