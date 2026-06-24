package org.ymir.lint.loading;

import org.ymir.global.GlobalMachineState;
import org.ymir.lexing.Word;
import org.ymir.lint.YILGlobal;
import org.ymir.lint.YILType;
import org.ymir.lint.global.YILConstant;
import org.ymir.lint.global.YILFrame;
import org.ymir.lint.global.YILGlobalVar;
import org.ymir.lint.instr.YILLabel;
import org.ymir.lint.value.YILVarValue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class YILLoader {

    private final Map<String, YILFrame> _frames = new HashMap<>();
    private final Map<String, YILConstant> _constants = new HashMap<>();
    private final Map<String, YILGlobalVar> _globalVars = new HashMap<>();

    private Instant _loadInstant = Instant.now();
    private byte[] _currentTypeTable = null;
    private byte[] _currentLocationTable = null;
    private byte[] _currentSymbolTable = null;
    private byte[] _currentStringTable = null;

    private Map<Long, YILType> _importedTypes = new HashMap<>();
    private Map<Long, String> _importedStrings = new HashMap<>();

    private Map<Long, YILLabel> _currentFrameLabels = new HashMap<>();
    private Map<Long, YILVarValue> _currentFrameVars = new HashMap<>();

    private boolean _inFrame = false;
    private final String _currentModule = "";

    public YILLoader() {
    }


    public Map<String, YILFrame> getFrames() {
        return _frames;
    }

    public Map<String, YILConstant> getConstants() {
        return _constants;
    }

    public Map<String, YILGlobalVar> getGlobalVars() {
        return _globalVars;
    }

    public boolean isEmpty() {
        return this._frames.isEmpty()
                && this._constants.isEmpty()
                && this._globalVars.isEmpty();
    }

    public List<YILGlobal> getNodes() {
        var constants = new java.util.ArrayList<YILGlobal>(this._constants.values().stream().toList());
        var frames = new java.util.ArrayList<YILGlobal>(this._frames.values().stream().toList());
        var vars = new java.util.ArrayList<YILGlobal>(this._globalVars.values().stream().toList());

        constants.sort(Comparator.comparing(YILGlobal::getName));
        frames.sort(Comparator.comparing(YILGlobal::getName));
        vars.sort(Comparator.comparing(YILGlobal::getName));

        return Stream.concat(
                        Stream.concat(constants.stream(),
                                frames.stream()),
                        vars.stream())
                .toList();
    }

    public void load(String module) throws YILLoadingError {
        try {
            try (var stream = new FileInputStream(module)) {
                var content = stream.readAllBytes();
                if (content.length == 0) {
                    return;
                }
                var reader = new ByteReader(content);
                var head = reader.readChar(3);
                int sizeByteSize = reader.readU32();
                var tableSizes = reader.readU64(4);

                if (head[0] != 'Y' || head[1] != 'I' || head[2] == 'L') {
                    throw new RuntimeException(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
                }

                if (tableSizes[2] == 0) {
                    return;
                }

                if (sizeByteSize != GlobalMachineState.POINTER_ARCH_SIZE) {
                    throw new RuntimeException(String.format(LoadingErrors.MISMATCH_ARCH_POINTER_SIZE.message, sizeByteSize, GlobalMachineState.POINTER_ARCH_SIZE));
                }

                var typeOff = 0;
                var locOff = typeOff + tableSizes[0];
                var symOff = locOff + tableSizes[1];
                var stringOff = symOff + tableSizes[2];

                this._currentTypeTable = ByteBuffer.wrap(content, typeOff, (int) tableSizes[0]).array();
                this._currentLocationTable = ByteBuffer.wrap(content, (int) locOff, (int) tableSizes[1]).array();
                this._currentSymbolTable = ByteBuffer.wrap(content, (int) symOff, (int) tableSizes[2]).array();
                this._currentStringTable = ByteBuffer.wrap(content, (int) stringOff, (int) tableSizes[3]).array();

                this._loadInstant = Instant.now();
                this._importedTypes = new HashMap<>();
                this._importedStrings = new HashMap<>();

                this.loadTypes();
                this.loadSymbols();
            }
        } catch (IOException io) {
            throw new YILLoadingError(String.format(LoadingErrors.FAILED_TO_PARSE_BYTE_FILE.message, module) + "\n" + io.getMessage());
        }
    }

    private void loadTypes() {
    }

    private void loadSymbols() throws YILLoadingError {
        var reader = new ByteReader(this._currentSymbolTable);
        while (reader.getSize() > 0) {
            var type = (int) reader.readU8();
            if (type == SymbolID.FRAME.value) {
                this.loadFrame(reader);
            } else if (type == SymbolID.CONSTANT.value) {
                this.loadConstant(reader);
            } else if (type == SymbolID.GLOBAL.value) {
                this.loadGlobal(reader);
            } else {
                throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_SYMBOL_TABLE.message));
            }
        }
    }

    private void loadFrame(ByteReader reader) throws YILLoadingError {
        var locOff = reader.readU64();
        var isWeak = reader.readBool();
        var nameOff = reader.readU64();
        var isGlbCtor = reader.readBool();
        var symLen = reader.readU64();

        var name = this.readString(nameOff);

    }


    private void loadGlobal(ByteReader reader) throws YILLoadingError {
    }

    private void loadConstant(ByteReader reader) throws YILLoadingError {

    }

    private String readString(long nameOff) throws YILLoadingError {
        if (this._importedStrings.containsKey(nameOff)) {
            return this._importedStrings.get(nameOff);
        }

        if (nameOff > this._currentStringTable.length) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_STRING_TABLE.message));
        }
        try {
            var reader = new ByteReader(ByteBuffer.wrap(this._currentStringTable, (int) nameOff, (int) (this._currentStringTable.length - nameOff)).array());
            var len = reader.readU64();
            var result = new String(reader.readChar((int) len));
            this._importedStrings.put(nameOff, result);

            return result;
        } catch (Throwable th) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_STRING_TABLE.message));
        }
    }

    private byte[] readStringAsU8(long nameOff) throws YILLoadingError {
        if (nameOff > this._currentStringTable.length) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_STRING_TABLE.message));
        }
        try {
            var reader = new ByteReader(ByteBuffer.wrap(this._currentStringTable, (int) nameOff, (int) (this._currentStringTable.length - nameOff)).array());
            var len = reader.readU64();
            return reader.readU8((int) len);
        } catch (Throwable th) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_STRING_TABLE.message));
        }
    }

    private Word readLocation(long locOff) throws YILLoadingError {
        if (locOff > this._currentLocationTable.length) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_STRING_TABLE.message));
        }

        try {
            var reader = new ByteReader(ByteBuffer.wrap(this._currentLocationTable, (int) locOff, (int) (this._currentLocationTable.length - locOff)).array());
            var strOff = reader.readU64();
            var line = reader.readU32();
            var col = reader.readU32();
            var file = this.readString(strOff);

            return new Word(file, line, col);
        } catch (Throwable th) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_STRING_TABLE.message));
        }
    }

}
