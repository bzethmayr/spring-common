package net.zethmayr.benjamin.spring.common.mapper.base;

interface FieldMapperTransform<T> {
    default String fieldName(final String fieldName) {
        return fieldName;
    }
}
