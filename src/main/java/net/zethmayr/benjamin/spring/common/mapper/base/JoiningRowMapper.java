package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
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
    private final List<MapperAndJoin> joinedMappers;
    private final List<MapperAndJoin<T, ?, ?>> topMappers;
    public final int topIndex;
    public final int lastIndex;
    private final InvertibleRowMapper[] allMappers;
    private final String selectEntire;
    private static final String LIST_SEP = ", ";

    @SafeVarargs
    private JoiningRowMapper(final InvertibleRowMapper<T> primary, final int initIndex, final MapperAndJoin<T, ?, ?>... joinedMappers) {
        int index = this.topIndex = initIndex;
        LOG.trace("new, topIndex is {}", topIndex);
        this.primary = rebindWithPrefix(primary, initIndex, index);
        this.joinedMappers = Collections.unmodifiableList(rebindAndFlatten(initIndex, joinedMappers));
        this.topMappers = topMappers(this.joinedMappers, topIndex);
        lastIndex = initIndex + this.joinedMappers.size();
        this.allMappers = new InvertibleRowMapper[this.joinedMappers.size() + 1];
        allMappers[0] = this.primary;
        for (int i = 1; i < allMappers.length; i++) {
            allMappers[i] = this.joinedMappers.get(i - 1).mapper();
        }
        selectEntire = generateSelectEntire(this.primary, this.joinedMappers);
        LOG.trace("generated JOIN select {}", selectEntire);
    }

    public List<MapperAndJoin> joinedMappers() {
        return joinedMappers;
    }

    public List<MapperAndJoin<T, ?, ?>> topMappers() {
        return topMappers;
    }

    @SuppressWarnings("unchecked") // we know the types of all top-level mappers
    private static <T> List<MapperAndJoin<T, ?, ?>> topMappers(final List<MapperAndJoin> joinedMappers, final int topIndex) {
        return joinedMappers.stream()
                .filter(m -> m.leftIndex() == topIndex)
                .map(m -> (MapperAndJoin<T,?,?>)m)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    protected JoiningRowMapper(final InvertibleRowMapper<T> primary, final MapperAndJoin<T, ?, ?>... joinedMappers) {
        this(primary, 0, joinedMappers);
        LOG.trace("subclass constructor end");
    }

    private List<MapperAndJoin> rebindAndFlatten(final int leftIndex, final MapperAndJoin... topJoinedMappers) {
        val flattened = new ArrayList<MapperAndJoin>();
        rebindAndFlatten(leftIndex, flattened, topJoinedMappers);
        return flattened;
    }

    private void rebindAndFlatten(final int leftIndex, final List<MapperAndJoin> list, final MapperAndJoin... topJoinedMappers) {
        LOG.trace("internal rebinding joins, leftIndex is {}", leftIndex);
        int index = leftIndex;
        for (int i = 0; i < topJoinedMappers.length; i++) {
            ++index;
            val mapperTransform = mapperTransform(leftIndex);
            val fieldTransform = fieldTransform(index);
            val transformedJoin = copyJoinTransforming(topJoinedMappers[i], mapperTransform, fieldTransform);
            list.add(transformedJoin);
            val joinedMapper = transformedJoin.mapper();
            if (joinedMapper instanceof JoiningRowMapper) {
                final JoiningRowMapper joiningMapper = (JoiningRowMapper)joinedMapper.copyTransforming(mapperTransform, fieldTransform);
                joiningMapper.rebindAndFlatten(index++, list, (MapperAndJoin[])joiningMapper.topMappers().toArray(new MapperAndJoin[]{}));
            }
        }
    }

    @Override
    public JoiningRowMapper<T> copyTransforming(final RowMapperTransform mapperTransform, final FieldMapperTransform fieldTransform) {
        LOG.trace("copying, leftIndex is {}, fieldTransform is {}", mapperTransform.leftIndex(), fieldTransform);
        MapperAndJoin[] copy = new MapperAndJoin[joinedMappers.size()];
        final int leftIndex = mapperTransform.leftIndex();
        for (int i = 0; i < copy.length; i++) {
            final MapperAndJoin<T, ?, ?> original = joinedMappers.get(i);
            copy[i] = copyJoinTransforming(original, mapperTransform, fieldTransform(leftIndex + 1 + i));
        }
        return new JoiningRowMapper<T>(primary.copyTransforming(mapperTransform, fieldTransform), fieldTransform.joinIndex(), copy) {
        };
    }

    private static <P, F, X> MapperAndJoin<P, F, X> copyJoinTransforming(final MapperAndJoin<P, F, X> original, final RowMapperTransform mapperTransform, final FieldMapperTransform fieldTransform) {
        LOG.trace("copying join, leftIndex is {}, fieldTransform is {}", mapperTransform.leftIndex(), fieldTransform);
        return original.toBuilder()
                // IntelliJ lombok plugin exhibits an issue, below. Going to, let it do so.
                .mapper(original.mapper().copyTransforming(mapperTransform, fieldTransform))
                .parentField((Mapper<P, ?, X>)original.parentField().copyTransforming(fieldTransform(mapperTransform.leftIndex())))
                .relatedField((Mapper<F, ?, X>)original.relatedField().copyTransforming(fieldTransform))
                //.leftIndex(mapperTransform.leftIndex())
                .leftIndex(mapperTransform.leftIndex())
                .build();
    }

    private static String prefix(final AtomicInteger index) {
        return prefix(index.get());
    }

    public static String prefix(final int index) {
        return "_" + index + "__";
    }

    private static <T> String generateSelectEntire(final InvertibleRowMapper<T> primary, final List<MapperAndJoin> joinedMappers) {
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
                                + " ON " + prefix(j.leftIndex()) + "." + j.parentField().fieldName()
                                + " " + j.relation().sql + " "
                                + prefix(initIndex) + "." + j.relatedField().fieldName
                                + "\n"
                        )
                        .collect(Collectors.joining())
                );
        return sb.toString();
    }

    private static <T> InvertibleRowMapper<T> rebindWithPrefix(final InvertibleRowMapper<T> original, final int leftIndex, final int joinIndex) {
        LOG.trace("static rebind of {} to leftIndex {}, joinIndex {}", original, leftIndex, joinIndex);
        return original.copyTransforming(
                mapperTransform(leftIndex),
                fieldTransform(joinIndex)
        );
    }

    private static RowMapperTransform mapperTransform(final int leftIndex) {
        LOG.trace("new mapper transform, leftIndex is {}", leftIndex);
        return new RowMapperTransform() {
            @Override
            public String table(final String table) {
                return table;
            }

            @Override
            public int leftIndex() {
                return leftIndex;
            }
        };
    }

    private static FieldMapperTransform fieldTransform(final int joinIndex) {
        LOG.trace("new field transform, joinIndex is {}", joinIndex);
        return new FieldMapperTransform() {
            @Override
            public String fieldName(final String fieldName) {
                return prefix(joinIndex) + fieldName;
            }
            @Override
            public int joinIndex() {
                return joinIndex;
            }
        };
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
        return extractDataInternal(rs, true);
    }

    private T extractDataInternal(ResultSet rs, final boolean allowAdvance) throws SQLException, DataAccessException {
        if (rs.isBeforeFirst()) {
            rs.next();
        }
        final T top = primary.mapRow(rs, rs.getRow());
        LOG.trace("top is {}", top);
        // Wait, might there not be any number of subordinates on the first row?
        final Map<Integer, Set<Object>> dedup = new HashMap<>();
        readJoined(rs, top, dedup);
        if (!allowAdvance) {
            return top;
        }
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
        for (int i = 0; i < topMappers.size(); i++) {
            InvertibleRowMapper<?> subMapper = topMappers.get(i).mapper();
            val idMapper = subMapper.idMapper();
            final Object subId = idMapper.from(rs);
            if (!Objects.isNull(subId)) {
                final Set<Object> existing = dedup.computeIfAbsent(i, (x) -> new HashSet<>());
                if (!existing.contains(subId)) {
                    existing.add(subId);
                    final Object sub = subMapper.mapRow(rs, rs.getRow());
                    if (sub != null) {
                        ((MapperAndJoin) topMappers.get(i)).acceptor().accept(top, subMapper.rowClass().cast(sub));
                    }
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int i) throws SQLException {
        return extractDataInternal(rs, false);
    }

    @Override
    public Object[] getInsertValues(T insert) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Mapper<T, ?, ?> idMapper() {
        return primary.idMapper();
    }
}
