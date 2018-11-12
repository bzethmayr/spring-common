package net.zethmayr.benjamin.spring.common.mapper.base;

import java.util.function.Function;

public interface FieldMapperTransform<T> {
    default String fieldName(final String fieldName) {
        return fieldName;
    }
}
