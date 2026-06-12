package org.ymir.lint.loading;

public enum ValueID {
    AFFECT(1),
    BLOCK(2),
    CALL(3),
    COND_JMP(4),
    GOTO(5),
    LABEL(6),
    RETURN(7),
    TRY_CATCH(8),
    TRY_FIN(9),
    VAR_DECL(10),
    ADDR_V(11),
    ARRAY_ACCESS_V(12),
    ARRAY_LIT_V(13),
    BEGIN_CATCH_V(14),
    BINARY_V(15),
    CAST_V(16),
    FIELD_V(17),
    FLOAT_V(18),
    INT_V(19),
    NAME_CALL_V(20),
    PTR_CALL_V(21),
    STRING_LIT_V(22),
    TUPLE_V(23),
    UNARY_V(24),
    UNIT_V(25),
    UNREF_V(26),
    VAR_V(27);

    public final int value;

    ValueID(int value) {
        this.value = value;
    }
}
