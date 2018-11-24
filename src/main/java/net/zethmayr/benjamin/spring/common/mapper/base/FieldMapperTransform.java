package net.zethmayr.benjamin.spring.common.mapper.base;

/**
 * A transformation to apply to a field mapper.
 * Used during joining mapper construction.
 */
interface FieldMapperTransform {
    /**
     * Returns a new result column name to bind to.
     * @return A new result column name to bind to.
     */
    default String fieldName(final String fieldName) {
        return fieldName;
    }

    /**
     * Returns the index value used in generating the result column name.
     * @return the index value used in generating the result column name.
     */
    default int joinIndex() {
        return 0;
    }
}
