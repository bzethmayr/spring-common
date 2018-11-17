package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;
import java.util.function.Predicate;

/**
 * A mapper
 * for any specific field of a given class.
 *
 * @param <C> the object type of which this maps a field
 */
/*
 * To avoid the inevitable confusion... this interface exists to loosen generic bounds.
 */
public abstract class ClassFieldMapper<C> {

    /**
     * Sub-classes do not have to provide any particular arguments at instantiation.
     */
    protected ClassFieldMapper() {}

    public abstract ClassFieldMapper<C> copyTransforming(final FieldMapperTransform<C> fieldTransform);

    /**
     * Returns the {@link ColumnType object which defines the field's SQL properties}.
     *
     * @return the {@link ColumnType object which defines the field's SQL properties}
     */
    public abstract ColumnType getColumnType();

    /**
     * Returns the JDBC serialization for the field.
     *
     * @param container The instance being mapped
     * @return the serialized field value
     */
    public abstract Object serFrom(C container);

    /**
     * Deserializes the field into the containing object from a {@link ResultSet}.
     *
     * @param container The instance being mapped into
     * @param rs        A ResultSet containing the field to map
     */
    public abstract Object desTo(C container, ResultSet rs);

    /**
     * Sets this field's position in a whole-row INSERT query.
     *
     * @param ordinal This field's position in a whole-row INSERT query.
     */
    abstract void setInsertOrdinal(int ordinal);

    /**
     * Returns this field's SQL name.
     *
     * @return This field's SQL name
     */
    public abstract String fieldName();

    /**
     * Returns this field's SQL type and constraints
     *
     * @return This field's SQL type and constraints
     */
    public abstract String sqlType();

    /**
     * Returns the SQL replacement character for queries, "?".
     *
     * @return The SQL replacement character for queries, "?"
     */
    public final String symbol() {
        return "?";
    }

    /**
     * Returns this field's position in a whole-row INSERT query
     *
     * @return This field's position in a whole-row INSERT query
     */
    public abstract int getInsertOrdinal();

    /**
     * Indicates whether a field is a generated index field, excluded from inserts.
     *
     * @param mapper The field mapper
     * @param <C>    The containing type
     * @return true if the field is a generated index, else false
     * @see ColumnType
     */
    public static <C> boolean isIndex(final ClassFieldMapper<C> mapper) {
        return mapper.getColumnType().isIndexColumn();
    }

    /**
     * This {@link Predicate} negates {@link #isIndex isIndex}.
     */
    public static final Predicate<ClassFieldMapper<?>> NOT_INDEX = (m) -> !isIndex(m);
}
