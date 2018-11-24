package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public abstract class JoiningRowMapper<T> implements InvertibleRowMapper<T> {

    public final InvertibleRowMapperBase<T> primary;
    private final List<MapperAndJoin> joinedMappers;
    private final List<MapperAndJoin<T, ?, ?>> topMappers;
    private final List<List<Integer>> mapperClears;
    public final int topIndex;
    public final int lastIndex;
    private final InvertibleRowMapper[] allMappers;
    private final String selectEntire;
    private static final String LIST_SEP = ", ";

    @SafeVarargs
    protected JoiningRowMapper(final InvertibleRowMapperBase<T> primary, final MapperAndJoin<T, ?, ?>... joinedMappers) {
        this(primary, null, 0, joinedMappers);
        LOG.trace("subclass constructor end");
    }

    @SafeVarargs
    private JoiningRowMapper(final InvertibleRowMapperBase<T> primary, final MapperAndJoin chainParent, final int initIndex, final MapperAndJoin<T, ?, ?>... joinedMappers) {
        int index = this.topIndex = initIndex;
        LOG.trace("new, topIndex is {}", topIndex);
        this.primary = rebindPrimary(primary, initIndex, index);
        this.joinedMappers = Collections.unmodifiableList(rebindAndFlatten(initIndex, chainParent, joinedMappers));
        this.mapperClears = findClears(this.joinedMappers);
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

    private static List<List<Integer>> findClears(final List<MapperAndJoin> joins) {
        val count = joins.size();
        val clears = new ArrayList<List<Integer>>(count);
        for (int outer = 0; outer < count; outer++) {
            val maybeLeft = joins.get(outer);
            val leftIndex = maybeLeft.leftIndex();
            val clear = new ArrayList<Integer>();
            for (int inner = 0; inner < count; inner++) {
                val maybeRight = joins.get(inner);
                if (maybeRight.leftIndex() > leftIndex) {
                    clear.add(inner);
                }
            }
            clear.trimToSize();
            clears.add(Collections.unmodifiableList(clear));
        }
        return Collections.unmodifiableList(clears);
    }

    public List<MapperAndJoin> joinedMappers() {
        return joinedMappers;
    }

    public List<MapperAndJoin<T, ?, ?>> topMappers() {
        return topMappers;
    }

    @SuppressWarnings("unchecked") // we know the parent types of all top-level mappers - it's our own type.
    private static <T> List<MapperAndJoin<T, ?, ?>> topMappers(final List<MapperAndJoin> joinedMappers, final int topIndex) {
        return joinedMappers.stream()
                .filter(m -> m.leftIndex() == topIndex)
                .map(m -> (MapperAndJoin<T,?,?>)m)
                .collect(Collectors.toList());
    }

    private List<MapperAndJoin> rebindAndFlatten(final int leftIndex, final MapperAndJoin chainParent, final MapperAndJoin... topJoinedMappers) {
        val flattened = new ArrayList<MapperAndJoin>();
        rebindAndFlatten(leftIndex, chainParent, flattened, topJoinedMappers);
        return flattened;
    }

    private void rebindAndFlatten(final int leftIndex, final MapperAndJoin chainParent, final List<MapperAndJoin> list, final MapperAndJoin... topJoinedMappers) {
        LOG.trace("internal rebinding joins, leftIndex is {}", leftIndex);
        int index = leftIndex;
        for (int i = 0; i < topJoinedMappers.length; i++) {
            ++index;
            val mapperTransform = mapperTransform(leftIndex);
            val fieldTransform = fieldTransform(index);
            val transformedJoin = copyJoinTransforming(topJoinedMappers[i], chainParent, mapperTransform, fieldTransform);
            list.add(transformedJoin);
            final InvertibleRowMapper joinedMapper = transformedJoin.mapper();
            if (joinedMapper instanceof JoiningRowMapper) {
                final JoiningRowMapper joiningMapper = (JoiningRowMapper)joinedMapper;
                joiningMapper.rebindAndFlatten(index++, transformedJoin, list, (MapperAndJoin[])joiningMapper.topMappers().toArray(new MapperAndJoin[]{}));
            }
        }
    }

    private static class Cloned<T> extends JoiningRowMapper<T> {
        protected Cloned(final InvertibleRowMapperBase<T> primary, final MapperAndJoin chainParent, MapperAndJoin<T, ?, ?>... joinedMappers) {
            super(primary, joinedMappers);
        }
    }

    @Override
    public JoiningRowMapper<T> copyTransforming(final RowMapperTransform mapperTransform, final FieldMapperTransform fieldTransform) {
        LOG.trace("copying, leftIndex is {}, fieldTransform is {}", mapperTransform.leftIndex(), fieldTransform);
        MapperAndJoin[] copy = new MapperAndJoin[topMappers.size()];
        final int leftIndex = mapperTransform.leftIndex();
        for (int i = 0; i < copy.length; i++) {
            final MapperAndJoin<T, ?, ?> original = topMappers.get(i);
            copy[i] = copyJoinTransforming(original, null, mapperTransform, fieldTransform(leftIndex + 1 + i));
        }
        return new JoiningRowMapper<T>(primary.copyTransforming(mapperTransform, fieldTransform), null, fieldTransform.joinIndex(), copy) {
        };
    }

    @SuppressWarnings("unchecked") // we adapt types in here, quite a bit, and as far as we know safely...
    private <P, F, X> MapperAndJoin copyJoinTransforming(final MapperAndJoin<P, F, X> original, final MapperAndJoin<T, P, ?> parent, final RowMapperTransform mapperTransform, final FieldMapperTransform fieldTransform) {
        LOG.trace("copying join, leftIndex is {}, fieldTransform is {}", mapperTransform.leftIndex(), fieldTransform);
        final BiConsumer<P, F> bareAcceptor = original.acceptor();
        final Supplier<MapperAndJoin.GetterState<P, F>> bareGetter = original.getter();
        final BiConsumer curriedAcceptor;
        final Supplier curriedGetter;
        if (parent != null) {
            LOG.trace("Currying acceptor over {}", bareAcceptor);
            final Function parentLast = parent.getter().get().getLast();
            curriedAcceptor = (t, f) -> bareAcceptor.accept((P)parentLast.apply(t), (F)f);
            curriedGetter = bareGetter.get().rebind(parentLast);
        } else {
            curriedAcceptor = bareAcceptor;
            curriedGetter = bareGetter;
        }
        return original.toBuilder()
                .mapper((InvertibleRowMapper)original.mapper().copyTransforming(mapperTransform, fieldTransform))
                .acceptor(curriedAcceptor)
                .getter(curriedGetter)
                .parentField((Mapper)original.parentField().copyTransforming(fieldTransform(mapperTransform.leftIndex())))
                .relatedField((Mapper)original.relatedField().copyTransforming(fieldTransform))
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

    private static <T> InvertibleRowMapperBase<T> rebindPrimary(final InvertibleRowMapperBase<T> original, final int leftIndex, final int joinIndex) {
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
    public Supplier<T> empty() {
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
        val basis = joinedMappers; // using topMappers works for up to one one-to-many traversal, but not more.
        for (int i = 0; i < basis.size(); i++) {
            val join = basis.get(i);
            final InvertibleRowMapper<?> subMapper = join.mapper();
            val idMapper = subMapper.idMapper();
            final Object subId = idMapper.from(rs);
            if (!Objects.isNull(subId)) {
                final Set<Object> existing = dedup.computeIfAbsent(i, (x) -> new HashSet<>());
                if (!existing.contains(subId)) {
                    for (int clear : mapperClears.get(i)) {
                        dedup.computeIfPresent(clear, (k, s) -> { s.clear(); return s; });
                    }
                    existing.add(subId);
                    final Object sub = subMapper.mapRow(rs, rs.getRow());
                    if (sub != null) {
                        // This is indeed a really unsafe call - it depends on having rewired the getters and acceptors properly
                        join.acceptor().accept(top, subMapper.rowClass().cast(sub));
                    }
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int i) throws SQLException {
        return primary.mapRow(rs, i);
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
