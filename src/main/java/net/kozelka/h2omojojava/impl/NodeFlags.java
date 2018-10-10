package net.kozelka.h2omojojava.impl;

/**
 * Immutable representation of all information contained inside "node type" byte.
 */
public class NodeFlags {
    /**
     * 8 bits representing 4 properties.
     * The bit pattern is "RRLLEENN" where
     * <ul>
     *     <li><b>RR</b> is either 11 indicating that right node is lead, or 00 if subtree</li>
     *     <li><b>LL</b> is either 11 indicating that left node is lead, or 00 if subtree</li>
     *     <li><b>EE</b> determines one of {@link SplitValueType}</li>
     *     <li><b>NN</b> determines number of bytes to skip if going right</li>
     * </ul>
     */
    public final byte flags;
    public final boolean leftNodeIsLeaf;
    public final boolean rightNodeIsLeaf;
    public final SplitValueType splitValueType;
    public final byte offsetSize;

    public NodeFlags(byte flags) {
        final int lmask = flags & 0x30;
        switch (lmask) {
            case 0x00: this.leftNodeIsLeaf = false; break;
            case 0x30: this.leftNodeIsLeaf = true; break;
            default: throw new UnsupportedOperationException(String.format("Invalid lmask value: 0x%02X", lmask));
        }
        final int rmask = flags & 0xC0;
        switch (rmask) {
            case 0x00: this.rightNodeIsLeaf = false; break;
            case 0xC0: this.rightNodeIsLeaf = true; break;
            default: throw new UnsupportedOperationException(String.format("Invalid rmask value: 0x%02X", rmask));
        }

        this.offsetSize = (byte)((flags & 0x03) + 1);
        this.flags = flags;
        this.splitValueType = SplitValueType.valueOf(flags);
    }

    public enum SplitValueType {
        NUMBER((byte) 0x00),
        BITSET((byte) 0x0C),
        BITSET_32((byte) 0x08);
        final byte equal;

        SplitValueType(byte equal) {
            this.equal = equal;
        }

        public static SplitValueType valueOf(byte flags) {
            final byte e = (byte) (flags & 0x0C);
            for (SplitValueType value : values()) {
                if (value.equal == e) return value;
            }
            throw new IllegalArgumentException(String.format("Flags %02X contain invalid equal bits: %02X",
                flags, e));
        }
    }
}
