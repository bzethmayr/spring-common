package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.model.base.ModelTrusted;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.NOT_INDEX;
import static net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper.isIndex;

public abstract class InvertibleRowMapper<T> extends ModelTrusted<InvertibleRowMapper> implements RowMapper<T> {

    private final Class<T> rowClass;
    private final List<ClassFieldMapper<T>> fields;
    private final String table;
    private final String selectMappable;
    private final String insert;

    protected InvertibleRowMapper(final Class<T> rowClass, final List<ClassFieldMapper<T>> fields, final String table, final String selectMappable, final String insert) {
        this.rowClass = rowClass;
        this.fields = Collections.unmodifiableList(fields);
        int i = 1;
        for (ClassFieldMapper<?> m : fields) {
            if (!isIndex(m)) {
                m.setInsertOrdinal(i);
                ++i;
            }
        }
        this.table = table;
        this.selectMappable = selectMappable;
        this.insert = insert;
    }

    public final Class<T> rowClass() {
        return rowClass;
    }

    public final List<ClassFieldMapper<T>> fields() {
        return fields;
    }

    public final String table() {
        return table;
    }

    public final String select() {
        return selectMappable;
    }

    public final String insert() {
        return insert;
    }

    public abstract T empty();

    protected static <T> String genSelect(final List<ClassFieldMapper<T>> fields, final String table) {
        return "SELECT " +
                fields.stream()
                        .map(ClassFieldMapper::fieldName)
                        .collect(Collectors.joining(", ")) +
                " FROM " + table;
    }

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
    public T mapRow(final @Nullable ResultSet rs, final int i) throws SQLException {
        final T partial = empty();
        marshaling(partial, true);
        for (ClassFieldMapper<T> m : fields) {
            m.desTo(partial, rs);
        }
        marshaling(partial, false);
        return partial;
    }

    public final Object[] getInsertValues(final T insert) {
        return fields.stream()
                .filter(NOT_INDEX)
                .map((m)-> m.serFrom(insert))
                .toArray();
    }
}
