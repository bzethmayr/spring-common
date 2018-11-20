package net.zethmayr.benjamin.spring.common.mapper.base;

interface FieldMapperTransform {
    default String fieldName(final String fieldName) {
        return fieldName;
    }
}
