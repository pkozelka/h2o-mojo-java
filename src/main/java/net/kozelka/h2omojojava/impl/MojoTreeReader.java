package net.kozelka.h2omojojava.impl;

import org.codehaus.plexus.util.IOUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MojoTreeReader {
    private final byte[] bytes;
    private final int nclasses;
    private int position = 0;

    public MojoTreeReader(File treeFile, int nclasses) throws IOException {
        this.nclasses = nclasses;
        try (final InputStream is = new FileInputStream(treeFile);
             final ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            IOUtil.copy(is, os);
            bytes = os.toByteArray();
        }
    }

    private void skip(int n) {
        position += n;
    }
    private int get1U() {
        System.out.printf("%5d = 0x%1$04X %02X%n", position, bytes[position]);
        return bytes[position++] & 0xFF;
    }
    private int get2() {
        return get1U() | (get1U() << 8);
    }
    private int get3() {
        return get1U() | (get1U() << 8) | (get1U() << 16);
    }
    private int get4() {
        return get2() | (get2() << 16);
    }
    private float get4f() {
        return Float.intBitsToFloat(get4());
    }

    public List<MtrNode> process() {
        final List<MtrNode> result = new ArrayList<>();
        while (position < bytes.length) {
            final MtrNode node = readNode();
            result.add(node);
            System.out.println("node = " + node);
        }
        return result;
    }

    private MtrNode readNode() {
        final int addr = position;
        final int nodeType = get1U();
        final int colId = get2();
        final MtrNode node = new MtrNode(addr, (byte) nodeType, colId);
        if (colId == 0xFFFF) {
            node.setLeafValue(get4f());
            return node;
        }

        final NASplitDir naSplitDir = NASplitDir.valueOf(get1U());
        node.setNaSplitDir(naSplitDir);
        boolean naVsRest = naSplitDir == NASplitDir.NAvsREST;
        boolean leftward = naSplitDir == NASplitDir.NALeft || naSplitDir == NASplitDir.Left;

        int lmask = nodeType & 0x33;
        int equal = nodeType & 0x0C;

        // READ SPLIT VALUE
        if (naVsRest) {
            // NAs go left, numbers go right
        } else {
            // "equal" determines what will be the split value
            switch (equal) {
                case 0x00: // split by number
                    node.setSplitValueFloat(get4f());
                    break;
                case 0x08: // read 32 bits
                    skip(4); //TODO store them
                    break;
                case 0x0C: // read bits from n bytes
                {
                    int bitoff = get2();
                    int nbytes = get2();
                    skip(nbytes); //TODO store them
                    break;
                }
                default: throw new UnsupportedOperationException("equal=" + equal);
            }

        }

        // READ RIGHT NODE offset
        final int rno;
        switch (lmask) {
            case 0x00: rno = get1U(); break;
            case 0x01: rno = get2(); break;
            case 0x02: rno = get3(); break;
            case 0x03: rno = get4(); break;
            case 0x10: rno = nclasses < 256 ? 1 : 2; break;
            case 0x30: rno = 4; break;
            default: throw new UnsupportedOperationException("lmask=" + lmask);
        }
        node.setRightNodeAddress(position + rno);
        if ((lmask & 0x90) > 0) { ///// both lmask (0x30) and rmask (0xC0), lower of the 2 bits
            node.setLeafValue(get4f());
        }
        node.setLeftNodeAddress(position);

        // LEFT NODE follows
        return node;
    }


    /**
     * Split direction for missing values.
     *
     * Warning: If you change this enum, make sure to synchronize them with `hex.genmodel.algos.tree.NaSplitDir` in
     * package `h2o-genmodel`.
     */
    public enum NASplitDir {
        //never saw NAs in training
        None(0),     //initial state - should not be present in a trained model

        // saw NAs in training
        NAvsREST(1), //split off non-NA (left) vs NA (right)
        NALeft(2),   //NA goes left
        NARight(3),  //NA goes right

        // never NAs in training, but have a way to deal with them in scoring
        Left(4),     //test time NA should go left
        Right(5);    //test time NA should go right

        private int value;
        NASplitDir(int v) { this.value = v; }
        public int value() { return value; }

        public static NASplitDir valueOf(int value) {
            for (NASplitDir c : values()) {
                if (c.value == value) return c;
            }
            throw new IllegalArgumentException(String.format("0x%02X = %1$d", value & 0xFF));
        }
    }

}
