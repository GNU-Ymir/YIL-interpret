package org.ymir.lexing;

public record Word(
    String filename,
    int line,
    int column
) {
    public static final Word EOF = new Word("", -1, -1);
}
