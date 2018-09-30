package net.kozelka.h2omojojava.impl;

import lombok.Data;

import java.util.BitSet;

/**
 * Condition can be:
 * - v is NaN (float)
 * - v <= split (float) and v is not NaN
 * - v <= split (float) or v is NaN
 * - split (bitset) contains bit no. v
 */
@Data
public class MtrNode {
    private String level;
    private int address;

    /**
     * node flags
     * mask 0x10: if set, left subnode is a leaf and its float value follows; otherwise left subnode follows
     * mask 0x33: lmask - if < 4, the number of bytes to add to 1 for storing offset to right node content
     * mask 0x40: if set, right subnode is a leaf and its float value follows; otherwise right subnode follows
     */
    private byte u1NodeType;

    // condition definition
    private MojoTreeReader.NASplitDir splitType;
    private int splitColumnId;
    private Float splitValueFloat;
    private BitSet splitValueBitset;

    // "if" part
    private MtrNode leftNode;
    private Float leftLeafValue;

    // "else" part
    private int rightNodeAddress;
    private MtrNode rightNode;
    private Float rightLeafValue;

    public MtrNode(int address, byte nodeType, int colId) {
        this.address = address;

        this.u1NodeType = nodeType;
        this.splitColumnId = colId;
    }

}
