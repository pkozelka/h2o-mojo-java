package net.kozelka.h2omojojava.impl;

import org.codehaus.plexus.util.IOUtil;

import java.io.*;

public class MojoTreeReader {
    final byte[] bytes;
    int position = 0;
    private Explainer explainer = new Explainer(this);
    private final int nclasses;

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

    public MtrNode readRootNode() {
        try {
           return readSubNode(":");
        } catch (Exception e) {
            explainer.flush();
            final int cnt = bytes.length - position;
            position += cnt;
            explainer.explainBytes(cnt, e.getClass().getName() + ": " + e.getMessage());
            throw e;
        }

    }

    private MtrNode readSubNode(String level) {
        explainer.comment("> " + level);
        final int addr = position;
        final int nodeType = get1U();
        explainer.explainNodeType();

        final int colId = get2();

        final MtrNode node = new MtrNode(addr, (byte) nodeType, colId);
        node.setLevel(level);
        explainer.explainInteger(2, colId, "column ID");
        if (colId == 0xFFFF) {
            float leafValue = get4f();
            explainer.explainFloat(leafValue,"LEAF VALUE");
            node.setLeafValue(leafValue);
            return node;
        } else if (colId > 1000) {
            throw new IllegalStateException("too big column ID: " + colId);
        }

        int naSplitDirByte = get1U();
        final NASplitDir naSplitDir = NASplitDir.valueOf(naSplitDirByte);
        explainer.explainInteger(1, naSplitDirByte, "direction = " + naSplitDir);

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
                    float splitValue = get4f();
                    node.setSplitValueFloat(splitValue);
                    explainer.explainFloat(splitValue, "split value");
                    break;
                case 0x08: // read 32 bits
                    skip(4); //TODO store them
                    explainer.explainBytes(4, "short bitset");
                    break;
                case 0x0C: // read bits from n bytes
                {
                    int bitoff = get2();
                    explainer.explainInteger(2, bitoff, "[].bitoff");
                    int nbytes = get2();
                    explainer.explainInteger(2, nbytes, "[].nbytes");
                    skip(nbytes); //TODO store them
                    explainer.explainBytes(nbytes, "long bitset");
                    break;
                }
                default: throw new UnsupportedOperationException("equal=" + equal);
            }

        }

        // read RIGHT NODE OFFSET which actually is LEFT NODE CONTENT
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
        {
            int rightNodeAddress = position + rno;
            explainer.explainBytes(position - explainer.unexplainedPosition, String.format("offset=%d, right node address is %d = 0x%1$04X", rno, rightNodeAddress));
            node.setRightNodeAddress(rightNodeAddress);
        }

        if ((nodeType & 0x10)>0) { ///// both lmask (0x30) and rmask (0xC0), lower of the 2 bits
            float leafValue = get4f();
            node.setLeafValue(leafValue);
            explainer.explainFloat(leafValue, "left leaf value");
        } else {
            final MtrNode left = readSubNode(level + "L");
            node.setLeftNode(left);
        }
        //
        if ((nodeType & 0x40) == 0x40) { ///// both lmask (0x30) and rmask (0xC0), lower of the 2 bits
            float leafValue = get4f();
            node.setLeafValue(leafValue);
            explainer.explainFloat(leafValue, "right leaf value");
        } else {
            final MtrNode right = readSubNode(level + "r");
            node.setRightNode(right);
        }
        explainer.comment("< " + level);
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
            throw new IllegalArgumentException(String.format("NASplitDir from 0x%02X = %1$d", value & 0xFF));
        }
    }

}
