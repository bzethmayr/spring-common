package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.model.base.ModelTrusted;
import org.springframework.lang.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.NOT_INDEX;
import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.isIndex;

/**
 * A row mapper.
 *
 * @param <T> The instance type corresponding to a row.
 */
public abstract class InvertibleRowMapperBase<T> extends ModelTrusted<InvertibleRowMapperBase> implements InvertibleRowMapper<T> {

    private final Class<T> rowClass;
    private final List<ClassFieldMapper<T>> fields;
    private final String table;
    private final Supplier<T> empty;
    private final String selectMappable;
    private final String insert;
    private final Mapper<T, ?, ?> idMapper;

    /**
     * Internal (and copy) constructor.
     *
     * @param rowClass                      The row class
     * @param fields                        The field mappers
     * @param table                         The table name
     * @param empty                         A supplier of empty instances
     * @param allowNoId                     Whether to allow there to be no id mapper
     * @param selectMappable                The generated SELECT query
     * @param insert                        The generated INSERT query
     */
    @SuppressWarnings("unused")
    // for the time being, without the unused boolean we would collide with the deprecated constructor
    InvertibleRowMapperBase(
            final Class<T> rowClass,
            final List<ClassFieldMapper<T>> fields,
            final String table,
            final Supplier<T> empty,
            final boolean allowNoId,
            final String selectMappable,
            final String insert
    ) {
        this.rowClass = rowClass;
        this.empty = empty;
        this.fields = Collections.unmodifiableList(fields);
        int i = 1;
        for (ClassFieldMapper<?> m : fields) {
            if (!isIndex(m)) {
                m.setInsertOrdinal(i);
                ++i;
            }
        }
        if (!allowNoId) {
            idMapper = findIdMapper(fields);
        } else {
            idMapper = null;
        }
        this.table = table;
        this.selectMappable = selectMappable;
        this.insert = insert;
    }

    /**
     * Creates a new instance.
     *
     * @param rowClass The row class
     * @param fields   The field mappers
     * @param table    The table name
     * @param empty    The empty instance supplier
     */
    protected InvertibleRowMapperBase(final Class<T> rowClass, final List<ClassFieldMapper<T>> fields, final String table, final Supplier<T> empty) {
        this(rowClass, fields, table, empty, false, genSelect(fields, table), genInsert(fields, table));
    }

    /**
     * Creates a new instance with no ID mapper. This instance will not be able participate in joins.
     *
     * @param rowClass The row class
     * @param fields   The field mappers
     * @param table    The table name
     * @param empty    The empty instance supplier
     * @param allowNoId If true, allow ID mapper detection to fail. This instance will not be able to participate in joins.
     */
    @Deprecated
    protected InvertibleRowMapperBase(final Class<T> rowClass, final List<ClassFieldMapper<T>> fields, final String table, final Supplier<T> empty, final boolean allowNoId) {
        this(rowClass, fields, table, empty, allowNoId, genSelect(fields, table), genInsert(fields, table));
    }

    /**
     * Creates a new instance.
     *
     * @param rowClass       The row class
     * @param fields         The field mappers
     * @param table          The table name
     * @param selectMappable A SELECT query to retrieve mappable fields
     * @param insert         An INSERT query to insert settable fields
     */
    @Deprecated // for verbosely redundant redundant verbosity
    protected InvertibleRowMapperBase(final Class<T> rowClass, final List<ClassFieldMapper<T>> fields, final String table, final String selectMappable, final String insert) {
        this(rowClass, fields, table, () -> {
            try {
                return rowClass.getConstructor().newInstance();
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw MappingException.because(e);
            }
        }, true, selectMappable, insert);
    }

    @Override
    public final Supplier<T> empty() {
        return empty;
    }

    private static class Cloned<T> extends InvertibleRowMapperBase<T> {
        private Cloned(final Class<T> rowClass, final List<ClassFieldMapper<T>> fields, final String table, final Supplier<T> empty) {
            super(rowClass, fields, table, empty);
        }
    }

    @Override
    public InvertibleRowMapperBase<T> copyTransforming(final RowMapperTransform rowTransform, final FieldMapperTransform fieldTransform) {
        final String tableTransformed = rowTransform.table(this.table);
        final List<ClassFieldMapper<T>> fieldsTransformed = this.fields.stream()
                .map((field) -> field.copyTransforming(fieldTransform))
                .collect(Collectors.toList());

        return new Cloned<>(
                this.rowClass,
                fieldsTransformed,
                tableTransformed,
                this.empty
        );
    }

    @SuppressWarnings("unchecked") // All ClassFieldMappers are secretly Mappers.
    private static <T> Mapper<T, ?, ?> findIdMapper(final List<ClassFieldMapper<T>> fields) {
        return fields.stream()
                .filter((m) -> m.fieldName().equals("id") || isIndex(m))
                .map(Mapper.class::<T, Object, Object>cast)
                .findFirst()
                .orElseThrow(() -> MappingException.badSetup("No ID field"));
    }

    /**
     * Returns the mapper for the id field.
     *
     * @return The mapper for the id field
     */
    @Override
    public Mapper<T, ?, ?> idMapper() {
        return idMapper;
    }


    @Override
    public final Class<T> rowClass() {
        return rowClass;
    }

    @Override
    public final List<ClassFieldMapper<T>> fields() {
        return fields;
    }

    @Override
    public List<ClassFieldMapper<T>> mappableFields() {
        return fields;
    }

    @Override
    public final String table() {
        return table;
    }

    @Override
    public final String select() {
        return selectMappable;
    }

    @Override
    public final String insert() {
        return insert;
    }

    /**
     * Generates a SELECT query for all the mapped fields of the row.
     *
     * @param fields The field mappers
     * @param table  The table name
     * @param <T>    The row type
     * @return A SELECT query with no WHERE clause
     */
    protected static <T> String genSelect(final List<ClassFieldMapper<T>> fields, final String table) {
        return "SELECT " +
                fields.stream()
                        .map(ClassFieldMapper::fieldName)
                        .collect(Collectors.joining(", ")) +
                " FROM " + table;
    }

    /**
     * Generates an INSERT query for all non-generated mapped fields of the row.
     *
     * @param fields The field mappers
     * @param table  The table name
     * @param <T>    The row class
     * @return A parameterized INSERT query
     */
    protected static <T> String genInsert(final List<ClassFieldMapper<T>> fields, final String table) {
        return "INSERT INTO " + table + " (" +
                fields.stream()
                        .filter(NOT_INDEX)
                        .map(ClassFieldMapper::fieldName)
                        .collect(Collectors.joining(", ")) +
                ") VALUES (" +
                fields.stream()
                        .filter(NOT_INDEX)
                        .map(ClassFieldMapper::symbol)
                        .collect(Collectors.joining(", ")) +
                ")";
    }

    @Override
    public T mapRow(final @Nullable ResultSet rs, final int i) {
        final T partial = empty.get();
        boolean allNull = true;
        marshaling(partial, true);
        for (ClassFieldMapper<T> m : fields) {
            allNull &= Objects.isNull(m.desTo(partial, rs));
        }
        marshaling(partial, false);
        if (allNull) {
            return null;
        } else {
            return partial;
        }
    }

    @Override
    public final Object[] getInsertValues(final T insert) {
        return fields.stream()
                .filter(NOT_INDEX)
                .map((m) -> m.serFrom(insert))
                .toArray();
    }
}
