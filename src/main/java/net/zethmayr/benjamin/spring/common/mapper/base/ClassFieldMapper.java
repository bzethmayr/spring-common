package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;
import java.util.function.Predicate;

/**
 * A mapper
 * for any specific field of a given class.
 * @param <C> the object type of which this maps a field
 */
/*
 * To avoid the inevitable confusion... this interface exists to loosen generic bounds.
 */
public abstract class ClassFieldMapper<C> {
    /**
     * @return the {@link ColumnType object which defines the field's SQL properties}
     */
    public abstract ColumnType getColumnType();

    /**
     * Returns the JDBC serialization for the field.
     * @param container
     * @return
     */
    public abstract Object serFrom(C container);

    /**
     * Deserializes the field into the containing object from a {@link ResultSet}.
     * @param container
     * @param rs
     */
    public abstract void desTo(C container, ResultSet rs);

    /**
     * Sets this field's position in a whole-row INSERT query.
     * @param ordinal
     */
    abstract void setInsertOrdinal(int ordinal);

    /**
     * @return this field's SQL name
     */
    public abstract String fieldName();

    /**
     * @return this field's SQL type and constraints
     */
    public abstract String sqlType();

    /**
     * @return the SQL replacement character for queries, "?".
     */
    public final String symbol() {
        return "?";
    }

    /**
     * @return this field's position in a whole-row INSERT query
     */
    public abstract int getInsertOrdinal();

    public static <C> boolean isIndex(final ClassFieldMapper<C> mapper) {
        return mapper.getColumnType().isIndexColumn();
    }

    public static final Predicate<ClassFieldMapper<?>> NOT_INDEX = (m) -> !isIndex(m);
}
