package net.zethmayr.benjamin.spring.common.mapper.base;

import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    public Mapper<T, ?, Integer> idMapper() {
        return idMapper;
    }

    /**
     * @param rowClassToken  An instance of the enum being mapped
     * @param fields         The mappers for each field
     * @param table          The table being mapped onto
     * @param selectMappable The query to select ids
     * @param insert         The query to insert all fields
     */
    @SuppressWarnings("unchecked") // casting the class of an enum to that same class, will succeed
    protected EnumRowMapper(final T rowClassToken, final List<ClassFieldMapper<T>> fields, final String table, final String selectMappable, final String insert) {
        super(rowClassToken.getDeclaringClass(), fields, table, selectMappable, insert);
        this.rowClassToken = rowClassToken;
        idMapper = findIdMapper(fields);
    }

    @Override
    public EnumRowMapper<T> copyTransforming(final RowMapperTransform<T> rowTransform, final FieldMapperTransform<T> fieldTransform) {
        final String tableTransformed = rowTransform.table(table());
        final List<ClassFieldMapper<T>> fieldsTransformed = fields().stream().map((field) -> field.copyTransforming(fieldTransform)).collect(Collectors.toList());

        /*
         * EWWWWW... Probably we should accept a supplier rather than trail references around
         */
        final Supplier<T> empty = this::empty;

        return new EnumRowMapper<T>(
                rowClassToken,
                fieldsTransformed,
                tableTransformed,
                genSelect(fieldsTransformed, tableTransformed),
                genInsert(fieldsTransformed, tableTransformed)
        ) {

        };
    }

    /**
     * Returns the possible values of the enum type.
     *
     * @return The possible values of the enum type
     */
    public final T[] enumValues() {
        return rowClass().getEnumConstants();
    }

    /**
     * Enum instances are immutable, and the result of empty() is intended for mutation.
     *
     * @return never
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public final T empty() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public final T mapRow(ResultSet rs, int rowNum) throws SQLException {
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
