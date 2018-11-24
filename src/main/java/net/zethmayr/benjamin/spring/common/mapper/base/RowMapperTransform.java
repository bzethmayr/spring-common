package net.zethmayr.benjamin.spring.common.mapper.base;

/**
 * Specifies a transform to apply to a row mapper.
 */
public interface RowMapperTransform {
    /**
     * A prefixed version of the table name
     *
     * @param table The table name
     * @return The prefixed table name
     */
    default String table(String table) {
        return table;
    }

    /**
     * The table index immediately left of this table in the join
     *
     * @return The table index immediately left of this table in the join
     */
    default int leftIndex() {
        return 0;
    }
}
