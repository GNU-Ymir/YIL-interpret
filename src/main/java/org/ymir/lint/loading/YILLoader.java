package org.ymir.lint.loading;

import ch.obermuhlner.math.big.BigFloat;
import org.ymir.global.GlobalMachineState;
import org.ymir.lexing.Word;
import org.ymir.lint.YILGlobal;
import org.ymir.lint.YILInstr;
import org.ymir.lint.YILType;
import org.ymir.lint.YILValue;
import org.ymir.lint.global.YILConstant;
import org.ymir.lint.global.YILFrame;
import org.ymir.lint.global.YILGlobalVar;
import org.ymir.lint.instr.*;
import org.ymir.lint.type.*;
import org.ymir.lint.value.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
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


    /*!
     * ====================================================================================================
     * ====================================================================================================
     * ====================================          GETTERS          =====================================
     * ====================================================================================================
     * ====================================================================================================
     */

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

    /*!
     * ====================================================================================================
     * ====================================================================================================
     * =====================================          USAGE          ======================================
     * ====================================================================================================
     * ====================================================================================================
     */

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

    /*!
     * ====================================================================================================
     * ====================================================================================================
     * =====================================          TYPES          ======================================
     * ====================================================================================================
     * ====================================================================================================
     */

    private void loadTypes() throws YILLoadingError {
        var reader = new ByteReader(this._currentTypeTable);
        while (reader.getSize() > 0) {
            var type = (int) reader.readU8();
            if (type == TypeID.ARRAY.value) {
                this.readArrayType(reader);
            } else if (type == TypeID.POINTER.value) {
                this.readPointerType(reader);
            } else if (type == TypeID.TUPLE.value) {
                this.readTupleType(reader);
            } else {
                throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_TYPE_TABLE.message));
            }
        }

        this.wrapUpTypeReferences();
    }

    private void readArrayType(ByteReader reader) throws YILLoadingError {
        var id = reader.readU64();
        var len = reader.readU64();
        var innerId = reader.readU64();
        var inner = this.getTypeOrPlaceHolder(innerId);

        this._importedTypes.put(innerId, new YILArray(id, inner, len));
    }

    private void readPointerType(ByteReader reader) throws YILLoadingError {
        var id = reader.readU64();
        var innerId = reader.readU64();
        var inner = this.getTypeOrPlaceHolder(innerId);

        this._importedTypes.put(innerId, new YILPointer(id, inner));
    }

    private void readTupleType(ByteReader reader) throws YILLoadingError {
        var id = reader.readU64();
        var isUnion = reader.readBool();
        var isPacked = reader.readBool();

        var nbNames = reader.readU64();
        var nbFields = reader.readU64();

        var names = new ArrayList<String>();
        var inners = new ArrayList<YILType>();
        for (long i = 0; i < nbNames; i++) {
            var off = reader.readU64();
            names.add(this.readString(off));
        }
        for (long i = 0; i < nbFields; i++) {
            var off = reader.readU64();
            inners.add(this.getTypeOrPlaceHolder(off));
        }

        this._importedTypes.put(id, new YILTuple(id, inners, names, isUnion, isPacked));
    }

    private void wrapUpTypeReferences() throws YILLoadingError {
        try {
            this._importedTypes.forEach((key, value) -> {
                if (value instanceof YILArray array) {
                    if (array.getInner() instanceof YILTypePlaceholder hold) {
                        try {
                            array.setInner(this.getType(hold.getUid()));
                        } catch (YILLoadingError e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (value instanceof YILPointer pointer) {
                    if (pointer.getInner() instanceof YILTypePlaceholder hold) {
                        try {
                            pointer.setInner(this.getType(hold.getUid()));
                        } catch (YILLoadingError e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (value instanceof YILTuple tuple) {
                    var innerList = new ArrayList<>(tuple.getInners());
                    for (var i = 0; i < innerList.size(); i++) {
                        if (innerList.get(i) instanceof YILTypePlaceholder hold) {
                            try {
                                innerList.set(i, this.getType(hold.getUid()));
                            } catch (YILLoadingError e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    tuple.setInners(innerList, tuple.getFieldNames());
                }
            });
        } catch (RuntimeException err) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_TYPE_TABLE.message));
        }
    }

    private YILType getTypeOrPlaceHolder(long uid) throws YILLoadingError {
        if (uid >= TypeID.OFFSET.value) {
            var result = this._importedTypes.get(uid);
            if (result == null) {
                return new YILTypePlaceholder(uid);
            }

            return result;
        }

        return this.getScalarType(uid);
    }

    private YILType getType(long uid) throws YILLoadingError {
        if (uid >= TypeID.OFFSET.value) {
            var result = this._importedTypes.get(uid);
            if (result == null) {
                throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_TYPE_TABLE.message, uid));
            }

            return result;
        }

        return this.getScalarType(uid);
    }

    private YILType getScalarType(long uid) throws YILLoadingError {
        if (uid == TypeID.FLOAT_32.value) {
            return YILFloat.f32;
        }
        if (uid == TypeID.FLOAT_64.value) {
            return YILFloat.f64;
        }
        if (uid == TypeID.FLOAT_80.value) {
            return YILFloat.f80;
        }
        if (uid == TypeID.FLOAT_MAX.value) {
            return YILFloat.fsize;
        }
        if (uid == TypeID.UINT_8.value) {
            return YILInt.i8;
        }
        if (uid == TypeID.UINT_16.value) {
            return YILInt.i16;
        }
        if (uid == TypeID.UINT_32.value) {
            return YILInt.i32;
        }
        if (uid == TypeID.UINT_64.value) {
            return YILInt.i64;
        }
        if (uid == TypeID.UINT_MAX.value) {
            return YILInt.isize;
        }
        if (uid == TypeID.SINT_8.value) {
            return YILInt.u8;
        }
        if (uid == TypeID.SINT_16.value) {
            return YILInt.u16;
        }
        if (uid == TypeID.SINT_32.value) {
            return YILInt.u32;
        }
        if (uid == TypeID.SINT_64.value) {
            return YILInt.u64;
        }
        if (uid == TypeID.SINT_MAX.value) {
            return YILInt.usize;
        }
        if (uid == TypeID.VOID.value) {
            return YILVoid.YIL_VOID;
        }

        throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE_TYPE_TABLE.message));
    }

    /*!
     * ====================================================================================================
     * ====================================================================================================
     * ====================================          SYMBOLS          =====================================
     * ====================================================================================================
     * ====================================================================================================
     */

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
        var old = this._frames.get(name);
        var replace = this.mustReplaceSymbol(old, name, isWeak);
        if (!replace) {
            if (!isWeak) {
                IO.println("Ignoring strong frame " + name);
            }

            reader.ignore(symLen);
            return;
        }

        try {
            var loc = this.readLocation(locOff);
            var nbParams = reader.readU32();
            var vars = new ArrayList<YILVarDecl>();
            for (var i = 0; i < nbParams; i++) {
                vars.add(this.readVarDecl(reader));
            }

            var retType = this.getType(reader.readU64());
            this._currentFrameLabels = new HashMap<>();
            this._currentFrameVars = new HashMap<>();
            this._inFrame = true;

            var body = this.readInstruction(reader);
            var result = new YILFrame(this._loadInstant,
                    this._currentModule,
                    loc,
                    loc,
                    name,
                    isWeak,
                    vars,
                    retType,
                    body,
                    isGlbCtor);

            this._frames.put(name, result);
        } catch (YILLoadingError err) {
            throw new YILLoadingError(String.format(LoadingErrors.IMPORTING_FRAME.message, name) +
                    err.getMessage());
        }
    }


    private void loadConstant(ByteReader reader) throws YILLoadingError {
        var locOff = reader.readU64();
        var isWeak = reader.readBool();
        var nameOff = reader.readU64();
        var isLocal = reader.readBool();
        var symLen = reader.readU64();

        var name = this.readString(nameOff);

        var old = this._constants.get(name);
        var replace = this.mustReplaceSymbol(old, name, isWeak);
        if (!replace) {
            if (!isWeak) {
                IO.println("Ignoring strong constant " + name);
            }

            reader.ignore(symLen);
            return;
        }

        try {
            var loc = this.readLocation(locOff);
            this._currentFrameLabels = new HashMap<>();
            this._currentFrameVars = new HashMap<>();
            this._inFrame = false;

            var value = this.readValue(reader);
            var result = new YILConstant(this._loadInstant,
                    this._currentModule,
                    loc,
                    loc,
                    name,
                    isWeak,
                    isLocal,
                    value);
            this._constants.put(name, result);
        } catch (YILLoadingError err) {
            throw new YILLoadingError(String.format(LoadingErrors.IMPORTING_CONSTANT.message, name) +
                    err.getMessage());
        }
    }

    private void loadGlobal(ByteReader reader) throws YILLoadingError {
        var locOff = reader.readU64();
        var isWeak = reader.readBool();
        var nameOff = reader.readU64();
        var isLocal = reader.readBool();
        var symLen = reader.readU64();

        var name = this.readString(nameOff);

        var old = this._globalVars.get(name);
        var replace = this.mustReplaceSymbol(old, name, isWeak);
        if (!replace) {
            if (!isWeak) {
                IO.println("Ignoring strong global var " + name);
            }

            reader.ignore(symLen);
            return;
        }

        try {
            var loc = this.readLocation(locOff);
            this._currentFrameLabels = new HashMap<>();
            this._currentFrameVars = new HashMap<>();
            this._inFrame = false;

            var value = this.readValue(reader);
            var result = new YILGlobalVar(this._loadInstant,
                    this._currentModule,
                    loc,
                    loc,
                    name,
                    isWeak,
                    isLocal,
                    value);
            this._globalVars.put(name, result);
        } catch (YILLoadingError err) {
            throw new YILLoadingError(String.format(LoadingErrors.IMPORTING_GLOBAL_VAR.message, name) +
                    err.getMessage());
        }
    }

    private boolean mustReplaceSymbol(YILGlobal old, String name, boolean isWeak) {
        boolean replace;
        if (old != null) {
            if (!isWeak) {
                replace = old.isWeak() || this._loadInstant.isAfter(old.getCreationTime());
            } else {
                replace = old.isWeak() && this._loadInstant.isAfter(old.getCreationTime());
            }
        } else {
            replace = true;
        }

        return replace;
    }

    /*!
     * ====================================================================================================
     * ====================================================================================================
     * ==================================          INSTRUCTIONS          ==================================
     * ====================================================================================================
     * ====================================================================================================
     */

    private YILInstr readInstruction(ByteReader reader) throws YILLoadingError {
        var code = reader.readU32();
        if (code == ValueID.AFFECT.value) {
            return this.readAffect(reader);
        }
        if (code == ValueID.BLOCK.value) {
            return this.readBlock(reader);
        }
        if (code == ValueID.CALL.value) {
            return this.readCall(reader);
        }
        if (code == ValueID.COND_JMP.value) {
            return this.readCondJmp(reader);
        }
        if (code == ValueID.GOTO.value) {
            return this.readGoto(reader);
        }
        if (code == ValueID.LABEL.value) {
            return this.readLabel(reader);
        }
        if (code == ValueID.RETURN.value) {
            return this.readReturn(reader);
        }
        if (code == ValueID.TRY_CATCH.value) {
            return this.readTryCatch(reader);
        }
        if (code == ValueID.TRY_FIN.value) {
            return this.readTryFin(reader);
        }
        if (code == ValueID.VAR_DECL.value) {
            return this.readVarDecl(reader);
        }

        throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
    }

    private YILVarDecl readVarDecl(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var name = this.readString(reader.readU64());
        var type = this.getType(reader.readU64());
        var id = reader.readU64();
        var isTemp = reader.readBool();

        return new YILVarDecl(loc, name, id, type, isTemp);
    }

    private YILInstr readTryFin(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var tryLen = reader.readU64();
        var finLen = reader.readU64();

        var tryPart = new ArrayList<YILInstr>();
        var finPart = new ArrayList<YILInstr>();
        for (int i = 0; i < tryLen; i++) {
            tryPart.add(this.readInstruction(reader));
        }
        for (int i = 0; i < finLen; i++) {
            finPart.add(this.readInstruction(reader));
        }

        return new YILTryFin(loc, tryPart, finPart);
    }

    private YILInstr readTryCatch(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());

        var tryLen = reader.readU64();
        var catchLen = reader.readU64();

        var tryPart = new ArrayList<YILInstr>();
        var catchPart = new ArrayList<YILInstr>();
        for (int i = 0; i < tryLen; i++) {
            tryPart.add(this.readInstruction(reader));
        }
        for (int i = 0; i < catchLen; i++) {
            catchPart.add(this.readInstruction(reader));
        }

        return new YILTryCatch(loc, type, tryPart, catchPart);
    }

    private YILInstr readReturn(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var value = this.readValue(reader);

        return new YILReturn(loc, value);
    }

    private YILInstr readLabel(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var id = reader.readU64();
        var name = this.readString(reader.readU64());

        YILLabel label = this._currentFrameLabels.get(id);
        if (label == null) {
            label = new YILLabel(loc, name, id);
            this._currentFrameLabels.put(id, label);
        }

        return label;
    }

    private YILInstr readGoto(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var thenLoc = this.readLocation(reader.readU64());

        var thenId = reader.readU64();
        var thenName = this.readString(reader.readU64());

        var thenLabel = this._currentFrameLabels.get(thenId);
        if (thenLabel == null) {
            thenLabel = new YILLabel(thenLoc, thenName, thenId);
            this._currentFrameLabels.put(thenId, thenLabel);
        }

        return new YILGoto(loc, thenLabel);
    }

    private YILInstr readCondJmp(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var thenLoc = this.readLocation(reader.readU64());
        var thenId = reader.readU64();
        var thenName = this.readString(reader.readU64());

        var elseLoc = this.readLocation(reader.readU64());
        var elseId = reader.readU64();
        var elseName = this.readString(reader.readU64());

        var test = this.readValue(reader);
        var thenLabel = this._currentFrameLabels.get(thenId);
        var elseLabel = this._currentFrameLabels.get(elseId);

        if (thenLabel == null) {
            thenLabel = new YILLabel(thenLoc, thenName, thenId);
            this._currentFrameLabels.put(thenId, thenLabel);
        }

        if (elseLabel == null) {
            elseLabel = new YILLabel(elseLoc, elseName, elseId);
            this._currentFrameLabels.put(elseId, elseLabel);
        }

        return new YILCondJump(loc, test, thenLabel, elseLabel);
    }

    private YILInstr readCall(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var value = this.readValue(reader);

        return new YILCall(loc, value);
    }

    private YILInstr readBlock(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var nb = reader.readU64();
        var instr = new ArrayList<YILInstr>();
        for (int i = 0; i < nb; i++) {
            instr.add(this.readInstruction(reader));
        }

        return new YILBlock(loc, instr);
    }

    private YILInstr readAffect(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var left = this.readValue(reader);
        var right = this.readValue(reader);

        return new YILAffect(loc, left, right);
    }

    /*!
     * ====================================================================================================
     * ====================================================================================================
     * =====================================          VALUES          =====================================
     * ====================================================================================================
     * ====================================================================================================
     */

    private YILValue readValue(ByteReader reader) throws YILLoadingError {
        var code = reader.readU32();
        if (code == ValueID.ADDR_V.value) {
            return this.readAddrValue(reader);
        }
        if (code == ValueID.ARRAY_ACCESS_V.value) {
            return this.readArrayAccessValue(reader);
        }
        if (code == ValueID.ARRAY_LIT_V.value) {
            return this.readArrayLiteral(reader);
        }
        if (code == ValueID.BEGIN_CATCH_V.value) {
            return this.readBeginCatch(reader);
        }
        if (code == ValueID.BINARY_V.value) {
            return this.readBinaryValue(reader);
        }
        if (code == ValueID.CAST_V.value) {
            return this.readCastValue(reader);
        }
        if (code == ValueID.FIELD_V.value) {
            return this.readFieldValue(reader);
        }
        if (code == ValueID.FLOAT_V.value) {
            return this.readFloatValue(reader);
        }
        if (code == ValueID.INT_V.value) {
            return this.readIntValue(reader);
        }
        if (code == ValueID.NAME_CALL_V.value) {
            return this.readNameCallValue(reader);
        }
        if (code == ValueID.PTR_CALL_V.value) {
            return this.readPtrCallValue(reader);
        }
        if (code == ValueID.STRING_LIT_V.value) {
            return this.readStringLiteral(reader);
        }
        if (code == ValueID.TUPLE_V.value) {
            return this.readTupleValue(reader);
        }
        if (code == ValueID.UNARY_V.value) {
            return this.readUnaryValue(reader);
        }
        if (code == ValueID.UNIT_V.value) {
            return this.readUnitValue(reader);
        }
        if (code == ValueID.UNREF_V.value) {
            return this.readUnrefValue(reader);
        }
        if (code == ValueID.VAR_V.value) {
            return this.readVarValue(reader);
        }

        throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
    }

    private YILValue readVarValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var name = this.readString(reader.readU64());
        var id = reader.readU64();
        var isThLocal = reader.readBool();
        var isTemp = reader.readBool();

        if (id != 0) {
            var localVar = this._currentFrameVars.get(id);
            if (localVar != null && this._inFrame) {
                if (!localVar.getType().opEquals(type)) {
                    throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
                }
                if (!localVar.getName().equals(name)) {
                    throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
                }
                if (localVar.isThreadLocal() != isThLocal) {
                    throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
                }
                if (localVar.isTemp() != isTemp) {
                    throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
                }

                if (localVar.getLoc().equals(loc)) {
                    return localVar;
                }
            }
        }

        var result = new YILVarValue(loc, type, id, name, isTemp, isThLocal);
        if (id != 0) {
            this._currentFrameVars.put(id, result);
        }

        return result;
    }

    private YILValue readUnitValue(ByteReader reader) {
        return YILUnitValue.YIL_UNIT;
    }

    private YILValue readUnrefValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var value = this.readValue(reader);

        return new YILUnrefValue(loc, type, value);
    }

    private YILValue readUnaryValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var opCode = reader.readU64();
        if (opCode >= UnaryOperator.values().length) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
        }

        var op = UnaryOperator.values()[(int) opCode];
        var value = this.readValue(reader);

        return new YILUnaryValue(loc, type, op, value);
    }

    private YILValue readTupleValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());

        var nbFieldNames = reader.readU64();
        var nbFieldValues = reader.readU64();

        var fieldNames = new ArrayList<String>();
        var fieldValues = new ArrayList<YILValue>();
        for (int i = 0; i < nbFieldNames; i++) {
            fieldNames.add(this.readString(reader.readU64()));
        }

        for (int i = 0; i < nbFieldValues; i++) {
            fieldValues.add(this.readValue(reader));
        }

        return new YILTupleValue(loc, type, fieldValues, fieldNames);
    }

    private YILValue readStringLiteral(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var content = this.readStringAsU8(reader.readU64());

        return new YILStringValue(loc, type, content);
    }

    private YILValue readPtrCallValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());

        var nbParams = reader.readU64();
        var ptr = this.readValue(reader);
        var params = new ArrayList<YILValue>();
        for (int i = 0; i < nbParams; i++) {
            params.add(this.readValue(reader));
        }

        return new YILPtrCallValue(loc, type, ptr, params);
    }

    private YILValue readNameCallValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var name = this.readString(reader.readU64());

        var nbParams = reader.readU64();
        var params = new ArrayList<YILValue>();
        for (int i = 0; i < nbParams; i++) {
            params.add(this.readValue(reader));
        }

        return new YILNameCallValue(loc, type, name, params);
    }

    private YILValue readIntValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());

        if (type instanceof YILInt iT) {
            var bigI = BigInteger.valueOf(reader.readU64());
            return new YILIntValue(loc, type, bigI);
        } else {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
        }
    }

    private YILValue readFloatValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var spec = reader.readU8();
        BigFloat.Context context = BigFloat.context(100); // 100 bits of precision

        var bigF = context.valueOf("0");
        if (spec == SpecialFloatValue.HEX.value) {
            var floatV = this.readString(reader.readU64());
            bigF = context.valueOf(floatV);
        } else if (spec == SpecialFloatValue.NAN.value) {
            bigF = BigFloat.NaN;
        } else if (spec == SpecialFloatValue.INFP.value) {
            bigF = BigFloat.POSITIVE_INFINITY;
        } else if (spec == SpecialFloatValue.INFN.value) {
            bigF = BigFloat.NEGATIVE_INFINITY;
        } else {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
        }

        return new YILFloatValue(loc, type, bigF);
    }

    private YILValue readFieldValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var isName = reader.readBool();
        var name = "";
        long index = 0;

        if (isName) {
            name = this.readString(reader.readU64());
        } else {
            index = reader.readU64();
        }

        var value = this.readValue(reader);
        return new YILFieldValue(loc, type, value, index, name);
    }

    private YILValue readCastValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var value = this.readValue(reader);

        return new YILCastValue(loc, type, value);
    }

    private YILValue readBinaryValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var opCode = reader.readU64();

        if (opCode >= BinaryOperator.values().length) {
            throw new YILLoadingError(String.format(LoadingErrors.MALFORMED_BYTECODE.message));
        }

        var op = BinaryOperator.values()[(int) opCode];
        var left = this.readValue(reader);
        var right = this.readValue(reader);

        return new YILBinaryValue(loc, type, left, right, op);
    }

    private YILValue readBeginCatch(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var name = this.readString(reader.readU64());

        return new YILBeginCatchValue(loc, name);
    }

    private YILValue readArrayLiteral(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());

        var len = reader.readU64();
        var values = new ArrayList<YILValue>();
        for (int i = 0; i < len; i++) {
            values.add(this.readValue(reader));
        }

        return new YILArrayValue(loc, type, values);
    }

    private YILValue readArrayAccessValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());

        var value = this.readValue(reader);
        var index = this.readValue(reader);

        return new YILArrayAccessValue(loc, type, index, value);
    }

    private YILValue readAddrValue(ByteReader reader) throws YILLoadingError {
        var loc = this.readLocation(reader.readU64());
        var type = this.getType(reader.readU64());
        var value = this.readValue(reader);

        return new YILAddrValue(loc, type, value);
    }

    /*!
     * ====================================================================================================
     * ====================================================================================================
     * ==================================          STRING TABLE          ==================================
     * ====================================================================================================
     * ====================================================================================================
     */


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
