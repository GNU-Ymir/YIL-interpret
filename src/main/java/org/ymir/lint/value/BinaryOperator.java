package org.ymir.lint.value;

public enum BinaryOperator {
    EQUAL("="),
    DPIPE("||"),
    DAND("&&"),
    INF("<"),
    SUP(">"),
    INF_EQUAL("<="),
    SUP_EQUAL(">="),
    NOT_EQUAL("!="),
    DEQUAL("=="),
    LEFTD("<<"),
    RIGHTD(">>"),
    PIPE("|"),
    XOR("^"),
    AND("&"),
    PLUS("+"),
    TILDE("~"),
    MINUS("-"),
    STAR("*"),
    PERCENT("%"),
    DIV("/");

    public final String content;

    BinaryOperator(String content) {
        this.content = content;
    }
}
