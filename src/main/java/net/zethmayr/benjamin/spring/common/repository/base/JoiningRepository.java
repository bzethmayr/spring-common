package net.zethmayr.benjamin.spring.common.repository.base;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.mapper.base.SqlOp;
import net.zethmayr.benjamin.spring.common.util.MapBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper.prefix;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.USE_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.GetterState.State.TERMINAL;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.INDEPENDENT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.NEEDS_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.PARENT_NEEDS_ID;
import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

/**
 * A repository for objects spanning multiple tables.
 *
 * @param <T> The persisted object type
 * @param <X> The index type
 */
@Slf4j
public abstract class JoiningRepository<T, X> implements Repository<T, X> {

    public final JdbcTemplate jdbcTemplate;
    public final JoiningRowMapper<T> mapper;
    public final MapperRepository<T, X> primary;
    public final List<Repository> supplemental;
    public final String getById;

    private final Map<MapperAndJoin, Repository> joinedRepositories;
    private final List<MapperAndJoin<T, ?, ?>> insertFirst;
    private final List<MapperAndJoin<T, ?, ?>> insertAfter;
    private final List<MapperAndJoin<T, ?, ?>> insertWhenever;
    private final List<MapperAndJoin<T, ?, ?>> deletePerId;
    private final List<MapperAndJoin<T, ?, ?>> deletePerInstance;

    /**
     * Subclass constructor.
     *
     * @param jdbcTemplate The JDBC template to use
     * @param mapper       A joining mapper
     * @param primary      A non-joining repository for the mapped type
     * @param supplemental Repositories for joined types - construction will fail unless sufficient are provided
     */
    protected JoiningRepository(final JdbcTemplate jdbcTemplate, final JoiningRowMapper<T> mapper, final MapperRepository<T, X> primary, final Repository... supplemental) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.primary = primary;
        this.supplemental = Arrays.asList(supplemental);
        joinedRepositories = correlateRepositories(mapper, primary, supplemental);
        getById = mapper.select() + "\nWHERE " + prefix(0) + "." + primary.idMapper.fieldName + " = ?";
        val topMappers = mapper.topMappers();
        insertFirst = Collections.unmodifiableList(topMappers.stream()
                .filter(m -> m.insertions() == PARENT_NEEDS_ID)
                .collect(Collectors.toList()));
        insertAfter = Collections.unmodifiableList(topMappers.stream()
                .filter(m -> m.insertions() == NEEDS_PARENT_ID)
                .collect(Collectors.toList()));
        insertWhenever = Collections.unmodifiableList(topMappers.stream()
                .filter(m -> m.insertions() == INDEPENDENT_INSERT)
                .collect(Collectors.toList()));
        deletePerId = Collections.unmodifiableList(topMappers.stream()
                .filter(m -> m.deletions() == USE_PARENT_ID)
                .collect(Collectors.toList()));
        deletePerInstance = Collections.unmodifiableList(topMappers.stream()
                .filter(m -> m.deletions() == MATERIALIZE_PARENT)
                .collect(Collectors.toList()));
    }

    private JoiningRepository(
            final JdbcTemplate jdbcTemplate,
            final JoiningRowMapper<T> mapper,
            final Mapper<T, ?, X> idMapper,
            final SqlOp parentRelation,
            final MapperRepository<T, X> primary,
            final List<Repository> supplemental,
            final Map<MapperAndJoin, Repository> joinedRepositories,
            final List<MapperAndJoin<T, ?, ?>> insertFirst,
            final List<MapperAndJoin<T, ?, ?>> insertAfter,
            final List<MapperAndJoin<T, ?, ?>> insertWhenever,
            final List<MapperAndJoin<T, ?, ?>> deletePerId,
            final List<MapperAndJoin<T, ?, ?>> deletePerInstance
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        getById = mapper.select() + "\nWHERE ? " + parentRelation.sql + " " + prefix(0) + "." + idMapper.fieldName;
        this.primary = primary;
        this.supplemental = supplemental;
        this.joinedRepositories = joinedRepositories;
        this.insertFirst = insertFirst;
        this.insertAfter = insertAfter;
        this.insertWhenever = insertWhenever;
        this.deletePerId = deletePerId;
        this.deletePerInstance = deletePerInstance;
    }

    private static class Cloned<T, X> extends JoiningRepository<T, X> {
        private Cloned(final JoiningRepository<T, X> toClone, final SqlOp relation, final Mapper<T, ?, X> idMapper) {
            super(toClone.jdbcTemplate, toClone.mapper, idMapper, relation,
                    toClone.primary.rebindWithRelatedIndex(relation, idMapper),
                    toClone.supplemental,
                    MapBuilder.<MapperAndJoin, Repository>identity().put(toClone.joinedRepositories)
                            .toEachValue((j, r) -> r.rebindWithRelatedIndex(j.relation(), j.relatedField()))
                            .build(),
                    toClone.insertFirst, toClone.insertAfter, toClone.insertWhenever,
                    toClone.deletePerId, toClone.deletePerInstance);
        }
    }

    @Override
    public JoiningRepository<T, X> rebindWithRelatedIndex(final SqlOp relation, Mapper<T, ?, X> idMapper) {
        return new Cloned<>(this, relation, idMapper);
    }

    private Map<MapperAndJoin, Repository> correlateRepositories(final JoiningRowMapper<T> mapper, final MapperRepository primary, final Repository... supplemental) {
        final Map<MapperAndJoin, Repository> joinedRepositories = new IdentityHashMap<>();
        for (val join : mapper.topMappers()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Looking at {} vs {}", join.mapper().rowClass(), primary.mapper().rowClass());
            }
            if (join.mapper().rowClass().equals(primary.mapper().rowClass())) {
                joinedRepositories.put(join, primary.rebindWithRelatedIndex(join.relation(), join.relatedField()));
            }
            for (val repo : supplemental) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Looking at {} vs {}", join.mapper().rowClass(), repo.mapper().rowClass());
                }
                if (join.mapper().rowClass().equals(repo.mapper().rowClass())) {
                    joinedRepositories.put(join, repo.rebindWithRelatedIndex(join.relation(), join.relatedField()));
                }
            }
            if (Objects.isNull(joinedRepositories.get(join))) {
                throw RepositoryException.badSetup("No repository for " + join + "!");
            }
        }
        LOG.trace("joinedRepositories is {}", joinedRepositories);
        return Collections.unmodifiableMap(joinedRepositories);
    }

    @SuppressWarnings("unchecked") // We correlate repositories on F, this is fine
    private <F, O> Repository<F, ?> getJoinedRepository(final MapperAndJoin<T, F, O> join) {
        return joinedRepositories.get(join);
    }

    @Override
    @Transactional(propagation = REQUIRED, isolation = REPEATABLE_READ, rollbackFor = Throwable.class)
    public X insert(T toInsert) {
        for (final MapperAndJoin<T, ?, ?> parentNeedsId : insertFirst) {
            internalInsertFirst(parentNeedsId, toInsert);
        }
        for (final MapperAndJoin<T, ?, ?> whenever : insertWhenever) {
            internalInsertWhenever(whenever, toInsert);
        }
        val primaryId = primary.insert(toInsert);
        for (final MapperAndJoin<T, ?, ?> needsParentId : insertAfter) {
            internalInsertAfter(needsParentId, toInsert);
        }
        return primaryId;
    }

    private <F, O> void internalInsertFirst(final MapperAndJoin<T, F, O> parentNeedsId, final T parent) {
        final MapperAndJoin.GetterState<T, F> getter = parentNeedsId.getter().get();
        switch (getter.state()) {
            case INIT_INSTANCE:
                internalInsert(parentNeedsId, parent, getter).ifPresent(inserted ->
                        parentNeedsId.parentField().desTo(parent, parentNeedsId.relatedField().serFrom(inserted))
                );
                break;
            case INIT_COLLECTION:
                throw RepositoryException.badSetup("Not sure which ID you were interested in...");
            default:
                throw new IllegalStateException("New getters should be in an INIT state");
        }
    }

    private <F, O> void internalInsertWhenever(final MapperAndJoin<T, F, O> whenever, final T parent) {
        final MapperAndJoin.GetterState<T, F> getter = whenever.getter().get();
        switch (getter.state()) {
            case INIT_INSTANCE:
                internalInsert(whenever, parent, getter);
                break;
            case INIT_COLLECTION:
                while (getter.state() != TERMINAL) {
                    internalInsert(whenever, parent, getter);
                }
                break;
            default:
                throw new IllegalStateException("New getters should be in an INIT state");
        }
    }

    private <F, O> void internalInsertAfter(final MapperAndJoin<T, F, O> needsParentId, final T parent) {
        final MapperAndJoin.GetterState<T, F> getter = needsParentId.getter().get();
        final Consumer<F> setter = (f) ->
                needsParentId.relatedField().desTo(f, needsParentId.parentField().serFrom(parent));
        switch (getter.state()) {
            case INIT_INSTANCE:
                internalInsert(needsParentId, parent, getter, setter);
                break;
            case INIT_COLLECTION:
                while (getter.state() != TERMINAL) {
                    internalInsert(needsParentId, parent, getter, setter);
                }
        }
    }

    private <F, O> Optional<F> internalInsert(final MapperAndJoin<T, F, O> join, final T parent, final MapperAndJoin.GetterState<T, F> getter) {
        return internalInsert(join, parent, getter, (f) -> {
        });
    }

    private <F, O> Optional<F> internalInsert(final MapperAndJoin<T, F, O> join, final T parent,
                                              final MapperAndJoin.GetterState<T, F> getter, final Consumer<F> mutator) {
        final Repository<F, ?> repo = getJoinedRepository(join);
        final F toInsert = getter.getter().apply(parent, getter);
        if (!Objects.isNull(toInsert)) {
            mutator.accept(toInsert);
            repo.insert(toInsert);
        }
        return Optional.ofNullable(toInsert);
    }

    @Override
    @Transactional(propagation = REQUIRED, isolation = REPEATABLE_READ, rollbackFor = Throwable.class)
    public void delete(X toDelete) {
        internalDeleteMaterialized(
                toDelete,
                deletePerInstance.size() > 0 ? getFor(toDelete) : Collections.emptyList()
        );
        primary.delete(toDelete);
    }

    @Override
    @Transactional(propagation = REQUIRED, isolation = REPEATABLE_READ, rollbackFor = Throwable.class)
    public void deleteMonadic(T toDelete) {
        internalDeleteMaterialized(
                deletePerId.size() > 0 ? ((Mapper<T, Object, X>) mapper().idMapper()).serFrom(toDelete) : null,
                Collections.singletonList(toDelete)
        );
        primary.deleteMonadic(toDelete);
    }

    private void internalDeleteMaterialized(final X idToDelete, final List<T> toDelete) {
        for (val deletePerParentId : deletePerId) {
            // deletion was re-bound to look at our ID
            val joinedRepo = (Repository) getJoinedRepository(deletePerParentId);
            joinedRepo.delete(idToDelete);
        }
        for (val existingParent : toDelete) {
            for (val deletePerFieldValue : deletePerInstance) {
                val joinedRepo = (Repository) getJoinedRepository(deletePerFieldValue);
                val getState = deletePerFieldValue.getter().get();
                final BiFunction getter = getState.getter();
                switch (getState.initialState()) {
                    case INIT_INSTANCE:
                        joinedRepo.deleteMonadic(getter.apply(existingParent, getState));
                        break;
                    case INIT_COLLECTION:
                        while (getState.state() != TERMINAL) {
                            joinedRepo.deleteMonadic(getter.apply(existingParent, getState));
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void deleteUnsafe(final String whereClause, final X id) {
        primary.deleteUnsafe(whereClause, id);
    }

    @Override
    public String insert() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InvertibleRowMapper<T> mapper() {
        return mapper;
    }

    @Override
    public String select() {
        return mapper.select();
    }

    @Override
    public Optional<T> getMonadic(T withId) {
        return get(primary.idMapper.serFrom(withId));
    }

    @Override
    public Optional<T> get(final X id) {
        try {
            return Optional.ofNullable(jdbcTemplate.query(getById, mapper.extractor(), id));
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getFor(X id) {
        try {
            return jdbcTemplate.query(getById, mapper.listExtractor(), id);
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getAll() {
        try {
            return jdbcTemplate.query(mapper.select(), mapper.listExtractor());
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }

    @Override
    public List<T> getUnsafe(final String sql, Object... values) {
        try {
            return jdbcTemplate.query(sql, mapper.listExtractor());
        } catch (RepositoryException rethrow) {
            throw rethrow;
        } catch (Exception e) {
            throw RepositoryException.because(e);
        }
    }
}
