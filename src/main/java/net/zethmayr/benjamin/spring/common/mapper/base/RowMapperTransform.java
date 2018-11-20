package net.zethmayr.benjamin.spring.common.mapper.base;

public interface RowMapperTransform {
    default String table(String table) {
        return table;
    }
    default int leftIndex() {
        return 0;
    }
}
