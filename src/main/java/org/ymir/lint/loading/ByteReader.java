package org.ymir.lint.loading;

public class ByteReader {

    private final byte[] _content;
    private long _cursor = 0;

    public ByteReader(byte[] content) {
        this._content = content;
    }

    public byte[] getAll() {
        return this._content;
    }

    public long getSize() {
        return this._content.length - this._cursor;
    }

    public void ignore(long nb) {
        this._cursor += nb;
        if (this._cursor >= this._content.length) {
            throw new RuntimeException("Malformed Byte Stream");
        }
    }

    public long readU64() {
        long current = this._cursor;
        this._cursor += 8;
        if (this._cursor > this._content.length) {
            throw new RuntimeException("Malformed Byte Stream");
        }

        long result = 0;
        result += ((long) this._content[(int) current]) & 0xFF;
        result += (((long) this._content[(int) current + 1]) & 0xFF) << 8;
        result += (((long) this._content[(int) current + 2]) & 0xFF) << 16;
        result += (((long) this._content[(int) current + 3]) & 0xFF) << 24;
        result += (((long) this._content[(int) current + 4]) & 0xFF) << 32;
        result += (((long) this._content[(int) current + 5]) & 0xFF) << 40;
        result += (((long) this._content[(int) current + 6]) & 0xFF) << 48;
        result += (((long) this._content[(int) current + 7]) & 0xFF) << 56;

        return result;
    }

    public long[] readU64(int n) {
        long[] result = new long[n];
        for (int i = 0; i < n; i++) {
            result[i] = readU64();
        }
        return result;
    }

    public int readU32() {
        long current = this._cursor;
        this._cursor += 4;
        if (this._cursor > this._content.length) {
            throw new RuntimeException("Malformed Byte Stream");
        }

        int result = 0;
        result += ((int) this._content[(int) current]) & 0xFF;
        result += (((int) this._content[(int) current + 1]) & 0xFF) << 8;
        result += (((int) this._content[(int) current + 2]) & 0xFF) << 16;
        result += (((int) this._content[(int) current + 3]) & 0xFF) << 24;

        return result;
    }

    public int[] readU32(int n) {
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = readU32();
        }
        return result;
    }

    public byte readU8() {
        if (this._cursor >= this._content.length) {
            throw new RuntimeException("Malformed Byte Stream");
        }

        this._cursor += 1;
        return this._content[(int) this._cursor - 1];
    }

    public byte[] readU8(int n) {
        byte[] result = new byte[n];
        for (int i = 0; i < n; i++) {
            result[i] = readU8();
        }
        return result;
    }

    public char readChar() {
        return (char) this.readU8();
    }

    public char[] readChar(int n) {
        char[] result = new char[n];
        for (int i = 0; i < n; i++) {
            result[i] = readChar();
        }
        return result;
    }

    public boolean readBool() {
        return this.readU8() != 0;
    }
}

