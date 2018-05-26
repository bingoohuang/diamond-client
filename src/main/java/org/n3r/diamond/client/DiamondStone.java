package org.n3r.diamond.client;


public class DiamondStone {
    private DiamondAxis diamondAxis;
    private String content;


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DiamondAxis getDiamondAxis() {
        return diamondAxis;
    }

    public void setDiamondAxis(DiamondAxis diamondAxis) {
        this.diamondAxis = diamondAxis;
    }

    @Override
    public String toString() {
        return "DiamondStone{" +
                "diamondAxis=" + diamondAxis +
                ", content=" + content +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiamondStone diamondStone = (DiamondStone) o;

        return diamondAxis != null ? diamondAxis.equals(diamondStone.diamondAxis) : diamondStone.diamondAxis == null;
    }

    @Override
    public int hashCode() {
        return diamondAxis != null ? diamondAxis.hashCode() : 0;
    }

}
