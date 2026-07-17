package org.ymir.lint.loading;

public enum SymbolID {
    FRAME(1),
    GLOBAL(2),
    CONSTANT(3);

    public final int value;

    SymbolID(int value) {
        this.value = value;
    }
}
