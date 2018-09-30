package net.kozelka.h2omojojava.impl;

import lombok.Data;

@Data
public class MtrNode {
    private String level;
    private int address;

    // node flags
    private byte u1NodeType;

    // condition definition
    private int u2ColumnId;
    private MojoTreeReader.NASplitDir naSplitDir;
    private Float splitValueFloat;

    private MtrNode leftNode;
    private Float leftLeafValue;

    private int rightNodeAddress;
    private MtrNode rightNode;
    private Float rightLeafValue;

    public MtrNode(int address, byte nodeType, int colId) {
        this.address = address;

        this.u1NodeType = nodeType;
        this.u2ColumnId = colId;
    }

}
