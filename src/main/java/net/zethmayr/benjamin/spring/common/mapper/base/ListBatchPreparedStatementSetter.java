package net.zethmayr.benjamin.spring.common.mapper.base;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.lang.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Sets values into a batch prepared statement from a list of row-equivalent objects
 * @param <T> The list  element type
 */
public class ListBatchPreparedStatementSetter<T> implements BatchPreparedStatementSetter {

    private final List<T> valueObjects;
    private final InvertibleRowMapper<T> rowMapper;

    /**
     * Creates a new instance to set values from the given list of instances using the given instance mapper
     * @param valueObjects A list of instances
     * @param mapper A row mapper
     */
    public ListBatchPreparedStatementSetter(final List<T> valueObjects, final InvertibleRowMapper<T> mapper) {
        this.valueObjects = valueObjects;
        this.rowMapper = mapper;
    }

    @Override
    @SuppressWarnings("unchecked") // methods of same mapper are known to match in type
    public void setValues(@Nullable PreparedStatement ps, int i) throws SQLException {
        final T value = valueObjects.get(i);
        rowMapper.fields().stream()
                .filter(Mapper.NOT_INDEX)
                .map(Mapper.class::cast)
                .forEach((m)->m.apply(ps, m.serFrom(value)));
    }

    @Override
    public int getBatchSize() {
        return valueObjects.size();
    }
}
