package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.val;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class JoiningRowMapper<T> implements InvertibleRowMapper<T> {

    private int mapperIndex = 0; // DANGER DANGER

    private final InvertibleRowMapper<T> primary;
    private final MapperAndJoin[] joinedMappers;
    private final InvertibleRowMapper[] allMappers;

    @SafeVarargs
    protected JoiningRowMapper(final InvertibleRowMapper<T> primary, final MapperAndJoin<T, ?>... joinedMappers) {
        int initIndex = 0;
        this.primary = rebindWithPrefix(primary, initIndex);
        this.joinedMappers = joinedMappers;
        this.allMappers = new InvertibleRowMapper[joinedMappers.length + 1];
        allMappers[0] = this.primary;
        for (int i = 0; i < joinedMappers.length; i++) {
            allMappers[i + 1] = rebindWithPrefix(joinedMappers[i].mapper(), ++initIndex);
        }
    }

    private static <T> InvertibleRowMapper<T> rebindWithPrefix(final InvertibleRowMapper<T> original, final int joinIndex) {
        return original.copyTransforming(
                new RowMapperTransform<T>() {
                    @Override
                    public String table(final String table) {
                        return joinIndex + "__" + table;
                    }
                },
                new FieldMapperTransform<T>() {
                    @Override
                    public String fieldName(final String fieldName) {
                        return joinIndex + "__" + fieldName;
                    }
                }
        );
    }

    @Override
    public Class<T> rowClass() {
        return null;
    }

    @Override
    public List<ClassFieldMapper<T>> fields() {
        return null;
    }

    @Override
    public String table() {
        switch (mapperIndex) {
            case -1:
                return "";
            default:
                return allMappers[mapperIndex].table();
        }
    }

    @Override
    public String select() {
        switch (mapperIndex) {
            case -1:
                return "";
            default:
                return allMappers[mapperIndex].select();
        }
    }

    @Override
    public String insert() {
        switch (mapperIndex) {
            case -1:
                return "";
            default:
                return allMappers[mapperIndex].insert();
        }
    }

    @Override
    public T empty() {
        return primary.empty();
    }

    public ResultSetExtractor<T> getExtractor() {
        return new ResultSetExtractor<T>() {
            @Override
            public T extractData(ResultSet rs) throws SQLException, DataAccessException {
                final T top = primary.mapRow(rs, rs.getRow());
                // Wait, might there not be any number of subordinates on the first row?
                for (int i = 0; i < joinedMappers.length; i++) {
                    val subMapper = allMappers[i + 1];
                    Object sub = subMapper.mapRow(rs, rs.getRow());
                    if (sub != null) {
                        joinedMappers[i].parentAcceptor().accept(top, subMapper.rowClass().cast(sub));
                    }
                }
                final Object id = primary.idMapper().from(rs);
                if (!Objects.isNull(top)) {
                    while (rs.next()) {
                        Object thisId = primary.idMapper().from(rs);
                        if (!id.equals(thisId)) {
                            rs.previous();
                            break;
                        } else {
                            // There might be any number of subordinates on any row.
                        }
                    }
                }
                return top;
            }
        };
    }

    @Override
    public T mapRow(ResultSet rs, int i) throws SQLException {
        // Augh. Should I cheat? I don't expect I want to force all this to materialize on one row...
        // Could try to cheat lightly, I suppose. One thing is though - we need more than one row.
        return getExtractor().extractData(rs);
    }

    @Override
    public Object[] getInsertValues(T insert) {
        switch (mapperIndex) {
            case -1:
                throw new IllegalStateException("Cannot get 'all' insert values, what would you do with them...");
            case 0:
                return primary.getInsertValues(insert);
            default:
                return joinedMappers[mapperIndex - 1].getInsertValues(insert);
        }
    }

    @Override
    public InvertibleRowMapper<T> copyTransforming(RowMapperTransform<T> mapperTransform, FieldMapperTransform<T> fieldTransform) {
        throw MappingException.badSetup("I can't deal with that.");
    }

    @Override
    public Mapper<T, ?, ?> idMapper() {
        return primary.idMapper();
    }
}
