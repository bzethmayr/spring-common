package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A type-safe mapper
 * for a specific field of a given class.
 * The sole concrete implementation is {@link ComposedMapper}.
 *
 * @param <C> the object type of which this maps a field, a.k.a the instance type
 * @param <I> The type of the class's mapped field, a.k.a. the instance field type or internal type
 * @param <O> The JDBC-level type of the mapped field, a.k.a. the external type or JDBC type
 * @see ComposedMapper
 */
public abstract class Mapper<C, I, O> extends ClassFieldMapper<C> implements SerMapper<I, O>, RsGetter<O>, PsSetter<O>, DesMapper<I, O> {

    /**
     * The SQL field name
     */
    public final String fieldName;

    /**
     * The SQL alias name, if any
     */
    public final String fieldAlias;

    /**
     * Construct a mapper for the given SQL field name.
     *
     * @param fieldName The field name
     */
    Mapper(final String fieldName) {
        this.fieldName = fieldName;
        this.fieldAlias = fieldName;
    }

    Mapper(final String fieldName, final String fieldAlias) {
        this.fieldName = fieldName;
        this.fieldAlias = fieldAlias;
    }

    public final String whereClause() {
        return " WHERE " + fieldAlias().replace("__","__.") + " = ?";
    }

    /**
     * @inheritDoc
     */
    @Override
    abstract void setInsertOrdinal(int ordinal);

    @Override
    public abstract int getInsertOrdinal();

    @Override
    public final String fieldName() {
        return fieldName;
    }

    @Override
    public final String fieldAlias() {
        return fieldAlias;
    }

    @Override
    public abstract ColumnType<O> getColumnType();

    @Override
    public abstract String sqlType();

    /**
     * @return The class object for the JDBC-level type of the field
     */
    public abstract Class<O> getExternalClass();

    /**
     * Retrieves the field value, in the internal type, from the containing class instance.
     *
     * @param container The containing instance
     * @return The field value in the internal type
     */
    public abstract I getFrom(final C container);

    /**
     * Retrieves the serialized field value from the containing object (in the external type).
     *
     * @param container The containing instance
     * @return The field value in the external type
     */
    @Override
    public O serFrom(final C container) {
        return ser(getFrom(container));
    }

    /**
     * Deserializes the field from a {@link ResultSet} into the containing object.
     *
     * @param container The containing instance
     * @param rs        The resultset providing the value
     * @return The field value in the internal type
     * @see ComposedMapper#from(ResultSet)
     */
    @Override
    public I desTo(final C container, final ResultSet rs) {
        return setTo(container, des(from(rs)));
    }

    /**
     * Deserializes a serialized value into the containing object's specific field
     *
     * @param container The containing instance
     * @param external  The field value in the external type
     */
    public void desTo(final C container, final O external) {
        setTo(container, des(external));
    }

    /**
     * Sets the field value on an instance of the class containing the field.
     *
     * @param container The containing instance
     * @param value     The field value in the internal type
     * @return The value that was set
     */
    public abstract I setTo(final C container, final I value);

    /**
     * Returns a setter for enum fields.
     * You can't set enum fields.
     *
     * @param <C> The enum type
     * @param <I> The type of value (it already has for that unmodifiable field)
     * @return A setter which throws when used.
     */
    public static <C, I> BiConsumer<C, I> cantSetTo() {
        return (ironBull, mosquito) -> {
            throw new UnsupportedOperationException();
        };
    }
}
