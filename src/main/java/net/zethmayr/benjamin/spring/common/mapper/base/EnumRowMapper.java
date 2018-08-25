package net.zethmayr.benjamin.spring.common.mapper.base;

import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

/**
 * A row mapper for tables which reflect enums -
 * writing is as normal,
 * but reading is id-only and materializes as the preexisting instance by ordinal.
 * @param <T> The mapped enum.
 */
public abstract class EnumRowMapper<T extends Enum<T>> extends InvertibleRowMapper<T> {

    private final Mapper<T, T, Integer> idMapper;

    /**
     * @param rowClass The enum being mapped
     * @param fields The mappers for each field
     * @param table The table being mapped onto
     * @param selectMappable The query to select ids
     * @param insert The query to insert all fields
     */
    protected EnumRowMapper(Class<T> rowClass, List<ClassFieldMapper<T>> fields, String table, String selectMappable, String insert) {
        super(rowClass, fields, table, selectMappable, insert);
        idMapper = findIdMapper(fields);
    }

    /**
     * @return the mapper for the id/ordinal field
     */
    public Mapper<T, T, Integer> idMapper() {
        return idMapper;
    }

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
     * @param fieldsIgnored ignored
     * @param table The table to generate the query for
     * @return a SELECT retrieving all ids
     */
    protected static String genSelectIds(final Object fieldsIgnored, final String table) {
        return "SELECT id FROM " + table;
    }
}
