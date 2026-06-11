package org.ymir.lint.value;

public enum UnaryOperator {
    MINUS("-"),
    AND("&"),
    STAR("*"),
    NOT("!");

    public final String content;

    UnaryOperator(String content) {
        this.content = content;
    }
}
