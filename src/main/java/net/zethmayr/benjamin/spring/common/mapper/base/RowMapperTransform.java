package net.zethmayr.benjamin.spring.common.mapper.base;

public interface RowMapperTransform<T> {
    default String table(String table) {
        return table;
    }
}
