package org.n3r.diamond.client;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.n3r.diamond.client.impl.Constants.DEFAULT_GROUP;

public class DiamondAxis {
    public String dataId;
    public String group;

    public static DiamondAxis makeAxis(String group, String dataId) {
        return new DiamondAxis(group, dataId);
    }

    public static DiamondAxis makeAxis(String dataId) {
        return new DiamondAxis(dataId);
    }

    public DiamondAxis(String dataId) {
        this(null, dataId);
    }

    public DiamondAxis(String group, String dataId) {
        if (isBlank(dataId)) throw new IllegalArgumentException("blank dataId");

        this.group = defaultIfEmpty(group, DEFAULT_GROUP);
        this.dataId = dataId;
    }

    public String getDataId() {
        return dataId;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "DiamondAxis{" +
                "dataId=" + dataId +
                ", group=" + group +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiamondAxis that = (DiamondAxis) o;

        if (!dataId.equals(that.dataId)) return false;
        if (!group.equals(that.group)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dataId.hashCode();
        result = 31 * result + group.hashCode();
        return result;
    }
}
