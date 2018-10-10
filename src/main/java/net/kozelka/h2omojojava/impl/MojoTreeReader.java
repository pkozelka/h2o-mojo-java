package net.kozelka.h2omojojava.impl;

import org.codehaus.plexus.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MojoTreeReader {
    final byte[] bytes;
    int position = 0;
    private Explainer explainer = new Explainer(this);
    private int mojoVersion;

    public MojoTreeReader(File treeFile, int mojoVersion) throws IOException {
        this.mojoVersion = mojoVersion;
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

    private NodeFlags parseNodeFlags() {
        final NodeFlags nodeFlags = new NodeFlags((byte) get1U());
        explainer.explainBytes(1, String.format("NODE left:%s, right:%s, splitBy:%s, offsetSize:%d",
            nodeFlags.leftNodeIsLeaf ? "LEAF" : "TREE",
            nodeFlags.rightNodeIsLeaf ? "LEAF" : "TREE",
            nodeFlags.splitValueType,
            nodeFlags.offsetSize
            ));
        return nodeFlags;
    }

    private int parseColumnId() {
        final int colId = get2();
        explainer.explainInteger(2, colId, "COLUMN");
        return colId;
    }

    private NASplitDir parseSplitDirection() {
        int naSplitDirByte = get1U();
        final NASplitDir naSplitDir = NASplitDir.valueOf(naSplitDirByte);
        explainer.explainInteger(1, naSplitDirByte, "DIRECTION." + naSplitDir);
        return naSplitDir;
    }

    private float parseLeafValue(String s) {
        float leafValue = get4f();
        explainer.explainFloat(leafValue, "LEAF." + s);
        if (leafValue < -10 || leafValue > 10) {
            throw new IllegalStateException(String.format("leaf value out of range: %f", leafValue));
        }
        return leafValue;
    }

    private int parseRightNodeAddress(NodeFlags nodeFlags) {
        // read RIGHT NODE OFFSET which actually is LEFT SIDE CONTENT size
        final int offset;
        switch (nodeFlags.offsetSize) {
            case 1: offset = get1U(); break;
            case 2: offset = get2(); break;
            case 3: offset = get3(); break;
            case 4: offset = get4(); break;
            default: throw new UnsupportedOperationException("offsetSize=" + nodeFlags.offsetSize);
        }
        final int rightNodeAddress = position + offset;
        explainer.explainBytes(position - explainer.unexplainedPosition, String.format("OFFSET %d  ; right node address is %d = 0x%2$08X", offset, rightNodeAddress));
        return rightNodeAddress;
    }

    private MtrNode readSubNode(String level) {
        explainer.comment("> " + level);
        final int addr = position;
        final NodeFlags nodeFlags = parseNodeFlags();

        final int colId = parseColumnId();

        final MtrNode node = new MtrNode(addr, nodeFlags, colId);
        node.setLevel(level);
        if (colId == 0xFFFF) {
            node.setLeftLeafValue(parseLeafValue("ROOT"));
            return node;
        } else if (colId > 100000) {
            throw new IllegalStateException("too big column ID: " + colId);
        }

        final NASplitDir naSplitDir = parseSplitDirection();
        node.setSplitType(naSplitDir);
        boolean naVsRest = naSplitDir == NASplitDir.NAvsREST;

        // READ SPLIT VALUE
        if (naVsRest) {
            // NAs go left, numbers go right
        } else {
            switch (nodeFlags.splitValueType) {
                case NUMBER: // split by number
                    node.setSplitValueFloat(parseSplitValue());
                    break;
                case BITSET_32: // read 32 bits
                    skip(4); //TODO store them
                    explainer.explainBytes(4, "BITSET_32");
                    break;
                case BITSET: // read bits from n bytes
                {
                    final int bitoff = get2();
                    explainer.explainInteger(2, bitoff, "BITSET.Offset");
                    if (mojoVersion < 130) {
                        final int nbytes = get2();
                        explainer.explainInteger(2, nbytes, "BITSET.Size.Bytes");
                        skip(nbytes); //TODO store them
                        explainer.explainBytes(nbytes, "BITSET.Content  ; OLD");
                    } else {
                        final int nbits = get4();
                        final int nbytes = bytes(nbits);
                        explainer.explainInteger(4, nbits, "BITSET.Size.Bits");
                        skip(nbytes); //TODO store them
                        explainer.explainBytes(nbytes, "BITSET.Content  ; V1.30");
                    }
                    break;
                }
                default: throw new UnsupportedOperationException("splitValueType is " + nodeFlags.splitValueType);
            }
        }

        if (nodeFlags.leftNodeIsLeaf) {
            node.setLeftLeafValue(parseLeafValue("Left"));
        } else {
            final int rightNodeAddress = parseRightNodeAddress(nodeFlags);
            node.setRightNodeAddress(rightNodeAddress);

            final MtrNode left = readSubNode(level + "L");
            node.setLeftNode(left);
        }
        //
        if (nodeFlags.rightNodeIsLeaf) {
            node.setRightLeafValue(parseLeafValue("Right"));
        } else {
            final MtrNode right = readSubNode(level + "r");
            node.setRightNode(right);
        }
        explainer.comment("< " + level);
        return node;
    }

    private float parseSplitValue() {
        float splitValue = get4f();
        explainer.explainFloat(splitValue, "SPLITVAL");
        return splitValue;
    }

    private static int bytes(int nbits) {
        return ((nbits-1) >> 3) + 1;
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
