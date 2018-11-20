package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public abstract class JoiningRowMapper<T> implements InvertibleRowMapper<T> {

    public final InvertibleRowMapper<T> primary;
    public final List<MapperAndJoin<T, ?, ?>> joinedMappers;
    private final InvertibleRowMapper[] allMappers;
    private final String selectEntire;
    private static final String LIST_SEP = ", ";

    @SafeVarargs
    protected JoiningRowMapper(final InvertibleRowMapper<T> primary, final MapperAndJoin<T, ?, ?>... joinedMappers) {
        int initIndex = 0;
        this.primary = rebindWithPrefix(primary, initIndex);
        this.joinedMappers = Collections.unmodifiableList(Arrays.asList(joinedMappers));
        this.allMappers = new InvertibleRowMapper[joinedMappers.length + 1];
        allMappers[0] = this.primary;
        for (int i = 0; i < joinedMappers.length; i++) {
            allMappers[i + 1] = rebindWithPrefix(joinedMappers[i].mapper(), ++initIndex);
        }
        selectEntire = generateSelectEntire(this.primary, this.joinedMappers);
        LOG.trace("generated JOIN select {}", selectEntire);
    }

    private static String prefix(final AtomicInteger index) {
        return prefix(index.get());
    }

    public static String prefix(final int index) {
        return "_" + index + "__";
    }

    private static <T> String generateSelectEntire(final InvertibleRowMapper<T> primary, final List<MapperAndJoin<T, ?, ?>> joinedMappers) {
        val sb = new StringBuilder();
        final AtomicInteger initIndex = new AtomicInteger(0);
        sb.append("SELECT \n")
                .append(primary.mappableFields().stream().map(f ->
                                prefix(initIndex) + "." + f.fieldName()
                                        + " AS " + prefix(initIndex) + f.fieldName()
                        ).collect(Collectors.joining(LIST_SEP))
                );
        for (int i = 0; i < joinedMappers.size(); i++) {
            initIndex.incrementAndGet();
            final InvertibleRowMapper<?> joinedMapper = joinedMappers.get(i).mapper();
            sb.append(LIST_SEP + "\n")
                    .append(joinedMapper.mappableFields().stream().map(f ->
                            prefix(initIndex) + "." + f.fieldName()
                                    + " AS " + prefix(initIndex) + f.fieldName()
                    ).collect(Collectors.joining(LIST_SEP)));
        }
        initIndex.set(0);
        sb.append("\nFROM ")
                .append(primary.table()).append(" ").append(prefix(initIndex))
                .append("\n")
                .append(joinedMappers.stream()
                        .map(j -> "LEFT JOIN " + j.mapper().table() + " " + prefix(initIndex.incrementAndGet())
                                + " ON " + prefix(0) + "." + j.parentField().fieldName()
                                + " " + j.relation().sql + " "
                                + prefix(initIndex) + "." + j.relatedField().fieldName
                                + "\n"
                        )
                        .collect(Collectors.joining())
                );
        return sb.toString();
    }

    private static <T> InvertibleRowMapper<T> rebindWithPrefix(final InvertibleRowMapper<T> original, final int joinIndex) {
        return original.copyTransforming(
                new RowMapperTransform<T>() {
                    @Override
                    public String table(final String table) {
                        return table;
                    }
                },
                new FieldMapperTransform<T>() {
                    @Override
                    public String fieldName(final String fieldName) {
                        return prefix(joinIndex) + fieldName;
                    }
                }
        );
    }

    @Override
    public Class<T> rowClass() {
        return primary.rowClass();
    }

    @Override
    public List<ClassFieldMapper<T>> fields() {
        return primary.fields();
    }

    @Override
    public List<ClassFieldMapper<T>> mappableFields() {
        return primary.mappableFields();
    }

    @Override
    public String table() {
        return primary.table();
    }

    @Override
    public String select() {
        return selectEntire;
    }

    @Override
    public String insert() {
        return primary.insert();
    }

    @Override
    public T empty() {
        return primary.empty();
    }

    public ResultSetExtractor<List<T>> listExtractor() {
        return rs -> {
            final List<T> results = new ArrayList<>();
            T one;
            if (rs.next()) do {
                one = extractDataInternal(rs);
                if (!Objects.isNull(one)) {
                    results.add(one);
                }
            } while (!rs.isAfterLast());
            return results;
        };
    }

    public ResultSetExtractor<T> extractor() {
        return this::extractDataInternal;
    }

    private T extractDataInternal(ResultSet rs) throws SQLException, DataAccessException {
        if (rs.isBeforeFirst()) {
            rs.next();
        }
        final T top = primary.mapRow(rs, rs.getRow());
        LOG.trace("top is {}", top);
        // Wait, might there not be any number of subordinates on the first row?
        final Map<Integer, Set<Object>> dedup = new HashMap<>();
        readJoined(rs, top, dedup);
        final Object id = primary.idMapper().from(rs);
        if (!Objects.isNull(top)) {
            while (rs.next()) {
                Object thisId = primary.idMapper().from(rs);
                if (!id.equals(thisId)) {
                    break;
                } else {
                    readJoined(rs, top, dedup);
                }
            }
        }
        return top;
    }

    private void readJoined(final ResultSet rs, final T top, final Map<Integer, Set<Object>> dedup) throws SQLException {
        for (int i = 0; i < joinedMappers.size(); i++) {
            InvertibleRowMapper<?> subMapper = allMappers[i + 1];
            val idMapper = subMapper.idMapper();
            final Object subId = idMapper.from(rs);
            if (!Objects.isNull(subId)) {
                final Set<Object> existing = dedup.computeIfAbsent(i, (x) -> new HashSet<>());
                if (!existing.contains(subId)) {
                    existing.add(subId);
                    final Object sub = subMapper.mapRow(rs, rs.getRow());
                    if (sub != null) {
                        ((MapperAndJoin) joinedMappers.get(i)).acceptor().accept(top, subMapper.rowClass().cast(sub));
                    }
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int i) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] getInsertValues(T insert) {
        throw new UnsupportedOperationException();
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
