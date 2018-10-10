package net.kozelka.h2omojojava.impl;

/**
 * Immutable representation of all information contained inside "node type" byte.
 */
public class NodeFlags {
    public final byte flags;
    public final boolean leftNodeIsLeaf;
    public final boolean rightNodeIsLeaf;
    public final SplitValueType splitValueType;

    public NodeFlags(byte flags) {
        this.flags = flags;
        this.leftNodeIsLeaf = (flags & 0x10) > 0;
        this.rightNodeIsLeaf = (flags & 0x40) > 0;
        this.splitValueType = SplitValueType.valueOf(flags);
    }

    public enum SplitValueType {
        NUMBER((byte) 0x00),
        BITSET((byte) 0x0C),
        MINI_BITSET((byte) 0x08);
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
