package org.ymir.lint.loading;

public enum LoadingErrors {
    FAILED_TO_CREATE_OUT_DIR("Failed to create YIL output directory %s, permission denied"),
    FAILED_TO_READ_BYTE_FILE("Failed to read YIL byte file %s, file not found or permission denied"),
    FAILED_TO_PARSE_BYTE_FILE("Failed to read YIL byte file %s, file format error"),
    FAILED_TO_WRITE_BYTE_FILE("Failed to write YIL byte file %s, permission denied"),
    MALFORMED_BYTECODE("Malformed bytecode"),
    MALFORMED_BYTECODE_TYPE_TABLE("Malformed bytecode, type table invalid"),
    MALFORMED_BYTECODE_STRING_TABLE("Malformed bytecode, string table invalid"),
    MALFORMED_BYTECODE_SYMBOL_TABLE("Malformed bytecode, symbol table invalid"),
    MALFORMED_BYTECODE_LOCATION_TABLE("Malformed bytecode, location table invalid"),
    MISMATCH_ARCH_POINTER_SIZE("YIL byte file was created for a %d bits target, mismatch current target arch %d bits"),
    IMPORTING_FRAME("import frame symbol %s"),
    IMPORTING_CONSTANT("import constant symbol %s"),
    IMPORTING_GLOBAL_VAR("import global variable symbol %s");

    public final String message;

    LoadingErrors(String message) {
        this.message = message;
    }
}
