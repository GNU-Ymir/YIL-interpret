package org.ymir.lint.loading;

public enum TypeID {
    FLOAT_32(1),
    FLOAT_64(2),
    FLOAT_80(3),
    FLOAT_MAX(4),
    SINT_8(5),
    SINT_16(6),
    SINT_32(7),
    SINT_64(8),
    SINT_MAX(9),
    UINT_8(10),
    UINT_16(11),
    UINT_32(12),
    UINT_64(13),
    UINT_MAX(14),
    VOID(15),
    ARRAY(16),
    TUPLE(17),
    POINTER(18),
    OFFSET(19);

    public final int value;

    TypeID(int value) {
        this.value = value;
    }
}
