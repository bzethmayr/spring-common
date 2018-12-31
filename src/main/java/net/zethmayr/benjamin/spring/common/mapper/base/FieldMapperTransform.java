package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * A transformation to apply to a field mapper.
 * Used during joining mapper construction.
 */
public interface FieldMapperTransform {
    /**
     * Returns a new result column name to bind to.
     *
     * @param fieldName The original result column name
     * @return A new result column name to bind to.
     */
    default String fieldName(final String fieldName) {
        return fieldName;
    }

    /**
     * Returns the index value used in generating the result column name.
     *
     * @return the index value used in generating the result column name.
     */
    default int joinIndex() {
        return 0;
    }
}
