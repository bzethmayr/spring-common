package net.zethmayr.benjamin.spring.common.mapper.base;

import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A row mapper for tables which reflect enums -
 * writing is as normal,
 * but reading is id-only and materializes as the preexisting instance by ordinal.
 * This exists to support referential integrity for ad-hoc queries.
 *
 * @param <T> The mapped enum.
 */
public abstract class EnumRowMapper<T extends Enum<T>> extends InvertibleRowMapperBase<T> {

    private final Mapper<T, T, Integer> idMapper;
    private final T rowClassToken;

    /**
     * Enum instances are immutable, and the result of empty() is intended for mutation.
     */
    private static final Supplier THROW = () -> {
        throw new UnsupportedOperationException();
    };

    @SuppressWarnings("unchecked") // the return type is not going to matter for this supplier
    private static <T> Supplier<T> thrower() {
        return (Supplier<T>) THROW;
    }

    @Override
    public Mapper<T, ?, Integer> idMapper() {
        return idMapper;
    }

    /**
     * Creates a new instance.
     *
     * @param rowClassToken Any instance of the enum being mapped
     * @param fields        The mappers for each field
     * @param table         The table being mapped onto
     */
    protected EnumRowMapper(final T rowClassToken, final List<ClassFieldMapper<T>> fields, final String table) {
        super(rowClassToken.getDeclaringClass(), fields, table, thrower(), false, genSelectIds(fields, table), genInsert(fields, table));
        this.rowClassToken = rowClassToken;
        idMapper = findIdMapper(fields);
    }

    /**
     * Creates a new instance.
     *
     * @param rowClassToken  An instance of the enum being mapped
     * @param fields         The mappers for each field
     * @param table          The table being mapped onto
     * @param selectMappable The query to select ids
     * @param insert         The query to insert all fields
     */
    @Deprecated // for verbosely redundant verbose redundancy
    protected EnumRowMapper(final T rowClassToken, final List<ClassFieldMapper<T>> fields, final String table, final String selectMappable, final String insert) {
        super(rowClassToken.getDeclaringClass(), fields, table, thrower(), false, selectMappable, insert);
        this.rowClassToken = rowClassToken;
        idMapper = findIdMapper(fields);
    }

    private static class Cloned<T extends Enum<T>> extends EnumRowMapper<T> {
        private Cloned(T rowClassToken, List<ClassFieldMapper<T>> fields, String table) {
            super(rowClassToken, fields, table);
        }
    }

    @Override
    public EnumRowMapper<T> copyTransforming(final RowMapperTransform rowTransform, final FieldMapperTransform fieldTransform) {
        return new Cloned<>(
                rowClassToken,
                fields().stream()
                        .map((field) -> field.copyTransforming(fieldTransform))
                        .collect(Collectors.toList()),
                rowTransform.table(table())
        );
    }

    @Override
    public final List<ClassFieldMapper<T>> mappableFields() {
        return Collections.singletonList(idMapper);
    }

    /**
     * Returns the possible values of the enum type.
     *
     * @return The possible values of the enum type
     */
    public final T[] enumValues() {
        return rowClass().getEnumConstants();
    }

    @Nullable
    @Override
    public final T mapRow(ResultSet rs, int rowNum) {
        return idMapper.des(idMapper.from(rs));
    }

    private static final Predicate<ClassFieldMapper> IS_ID_MAPPER = (m) -> "id".equals(m.fieldName());

    @SuppressWarnings("unchecked") // if you construct row mappers with incompatible id mappers, this will blow up
    private static <T> Mapper<T, T, Integer> findIdMapper(final List<ClassFieldMapper<T>> fields) {
        return fields.stream()
                .filter(IS_ID_MAPPER)
                .map(Mapper.class::<T, T, Integer>cast)
                .findFirst()
                .orElseThrow(() -> MappingException.badSetup("No id field"));
    }

    /**
     * Generates a SELECT query retrieving all ids from the table.
     *
     * @param fieldsIgnored ignored
     * @param table         The table to generate the query for
     * @return a SELECT retrieving all ids
     */
    protected static String genSelectIds(final Object fieldsIgnored, final String table) {
        return "SELECT id FROM " + table;
    }
}
