package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;
import java.util.function.BiConsumer;

/**
 * A typesafe mapper
 * for a specific field of a given class.
 * {@inheritDoc}
 *
 * @param <I> The type of the class's mapped field, a.k.a. the internal type
 * @param <O> The JDBC-level type of the mapped field, a.k.a. the external type
 */
public abstract class Mapper<C, I, O> extends ClassFieldMapper<C> implements SerMapper<I, O>, RsGetter<O>, PsSetter<O>, DesMapper<I, O> {

    public final String fieldName;

    /**
     * Construct a mapper for the given SQL field name.
     * @param fieldName
     */
    protected Mapper(final String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    abstract void setInsertOrdinal(int ordinal);

    @Override
    public abstract int getInsertOrdinal();

    @Override
    public final String fieldName() {
        return fieldName;
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
     * @param container The containing instance
     * @return The field value in the internal type
     */
    public abstract I getFrom(final C container);

    /**
     * Retrieves the serialized field value from the containing object (in the external type).
     * @param container The containing instance
     * @return The field value in the external type
     */
    @Override
    public O serFrom(final C container) {
        return ser(getFrom(container));
    }

    /**
     * Deserializes the field from a {@link ResultSet} directly into the containing object.
     * @param container The containing instance
     * @param rs The resultset providing the value
     */
    @Override
    public void desTo(final C container, final ResultSet rs) {
        setTo(container, des(from(rs)));
    }

    /**
     * Deserializes a serialized value into the containing object's specific field
     * @param container The containing instance
     * @param external The field value in the external type
     */
    public void desTo(final C container, final O external) {
        setTo(container, des(external));
    }

    /**
     * Sets the field value on an instance of the class containing the field.
     * @param container The containing instance
     * @param value The field value in the internal type
     */
    public abstract void setTo(final C container, final I value);

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
