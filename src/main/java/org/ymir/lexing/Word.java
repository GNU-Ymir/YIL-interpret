package org.ymir.lexing;

public record Word(
    String filename,
    int line,
    int column
) {}
