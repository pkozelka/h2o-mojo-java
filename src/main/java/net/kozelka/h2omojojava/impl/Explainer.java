package net.kozelka.h2omojojava.impl;

import java.util.Arrays;
import java.util.LinkedList;

public class Explainer {
    int unexplainedPosition = 0;
    private int unflushedPosition = 0;
    private LinkedList<String> descriptions = new LinkedList<>();

    private final MojoTreeReader mtr;

    public Explainer(MojoTreeReader mtr) {
        this.mtr = mtr;
    }

    private void describe(String format, Object... args) {
        final String s = String.format(format, args);
        descriptions.addAll(Arrays.asList(s.split("\n")));
    }

    void flush() {
        if (unexplainedPosition > mtr.position) {
            throw new IllegalStateException("" + unexplainedPosition);
        }
        // show disassembly output: first line = address, max 4 bytes, first description; then continue with more 4 bytes and/or more description lines

        // other lines if bytes or descriptions remain
        while (!descriptions.isEmpty() || unflushedPosition<unexplainedPosition) {
            final String addrPart = String.format("%5d = 0x%1$04X ", unflushedPosition);

            final StringBuilder hexPart = new StringBuilder();
            for (int i=0; i<8; i++) {
                if (unflushedPosition < unexplainedPosition) {
                    hexPart.append(String.format("%02X ", mtr.bytes[unflushedPosition++]));
                } else {
                    hexPart.append("   ");
                }
            }

            final String descPart = descriptions.isEmpty() ? "" : descriptions.removeFirst();
            if (descPart.startsWith("; > ")) {
                System.out.println();
            }
            System.out.printf("%s %s %s%n", addrPart, hexPart, descPart);
        }

        //
        unflushedPosition = unexplainedPosition;
        if (unexplainedPosition < mtr.position) {
            unexplainedPosition = mtr.position;
            describe("Junk");
            flush();
        }
    }

    public void explainNodeType() {
        byte u1NodeType = mtr.bytes[unexplainedPosition++];
        describe("=== NODE === Rmask=%02X Lmask=%02X equal=%02X)",
            u1NodeType & 0xC0,
            u1NodeType & 0x33,
            u1NodeType & 0x0C
        );
        flush();
    }

    public void explainFloat(float value, String desc) {
        describe("%s = %f (float)", desc, value);
        unexplainedPosition += 4;
        flush();
    }

    public void explainInteger(int bytecount, int value, String desc) {
        describe("%s = %d (integer)", desc, value);
        unexplainedPosition += bytecount;
        flush();
    }

    public void explainBytes(int bytecount, String desc) {
        describe("%s (%d bytes)", desc, bytecount);
        unexplainedPosition += bytecount;
        flush();
    }

    public void comment(String desc) {
        describe("; %s", desc);
        flush();
    }
}
