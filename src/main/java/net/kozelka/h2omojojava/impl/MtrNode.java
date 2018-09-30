package net.kozelka.h2omojojava.impl;

import lombok.Data;

import java.util.BitSet;

@Data
public class MtrNode {
    private String level;
    private int address;

    // node flags
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
