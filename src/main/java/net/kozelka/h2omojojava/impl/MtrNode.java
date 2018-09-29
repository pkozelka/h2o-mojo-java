package net.kozelka.h2omojojava.impl;

public class MtrNode {
    private final int address;
    private final byte u1NodeType;
    private final int u2ColumnId;

    private Float f4LeafValue = null;
    private MojoTreeReader.NASplitDir u1NaSplitDir;
    private Float f4SplitValueFloat;
    private int rightNodeAddress;
    private int leftNodeAddress;

    public MtrNode(int address, byte nodeType, int colId) {
        this.address = address;

        this.u1NodeType = nodeType;
        this.u2ColumnId = colId;
    }

    public void setLeafValue(Float leafValue) {
        this.f4LeafValue = leafValue;
    }

    public void setNaSplitDir(MojoTreeReader.NASplitDir naSplitDir) {

        u1NaSplitDir = naSplitDir;
    }

    public void setSplitValueFloat(float splitValueFloat) {
        this.f4SplitValueFloat = splitValueFloat;
    }

    public float getSplitValueFloat() {
        return f4SplitValueFloat;
    }

    public void setRightNodeAddress(int rightNodeAddress) {
        this.rightNodeAddress = rightNodeAddress;
    }

    public void setLeftNodeAddress(int leftNodeAddress) {
        this.leftNodeAddress = leftNodeAddress;
    }

    public int getRightNodeAddress() {
        return rightNodeAddress;
    }

    public int getLeftNodeAddress() {
        return leftNodeAddress;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MtrNode{");
        sb.append("address=").append(address);
        sb.append(String.format(", u1NodeType=0x%02X(R/L/E=%02X/%02X/%02X)",
            u1NodeType,
            u1NodeType & 0xC0,
            u1NodeType & 0x30,
            u1NodeType & 0x0C
            ));
        sb.append(", u2ColumnId=").append(u2ColumnId);
        sb.append(", f4LeafValue=").append(f4LeafValue);
        sb.append(", u1NaSplitDir=").append(u1NaSplitDir);
        sb.append(", rightNodeAddress=").append(rightNodeAddress);
        sb.append(", f4SplitValueFloat=").append(f4SplitValueFloat);
        sb.append('}');
        return sb.toString();
    }
}
