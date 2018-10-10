package net.kozelka.h2omojojava.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class consumes the underlying stream by pieces defined by caller.
 * Each piece consumed needs to explain the received data.
 */
public class BinaryEater {

    private final InputStream is;

    public BinaryEater(InputStream is) {
        this.is = is;
    }

    public byte[] readBytes(int cnt) throws IOException {
        if (cnt < 1) {
            throw new IllegalArgumentException("Must read at least one byte, caller requested " + cnt);
        }
        final byte[] bytes = new byte[cnt];
        final int result = is.read(bytes);
        if (result < cnt) {
            throw new IOException(String.format("Could not read %d bytes, only %d is available", cnt, result));
        }
        return bytes;
    }
}
