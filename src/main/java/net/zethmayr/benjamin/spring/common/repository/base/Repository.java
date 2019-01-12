package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.SqlOp;

import java.util.List;
import java.util.Optional;

/**
 * A persistent indexed object store over an SQL database table.
 *
 * @param <T> The type of object persisted
 * @param <X> The index type
 */
public interface Repository<T, X> {
    /**
     * Rebinds this repository to participate in a join - primarily internal usage.
     * @param relation A relation
     * @param idMapper The related mapper
     * @return A re-bound copy of this repository.
     */
    Repository<T, X> rebindWithRelatedIndex(final SqlOp relation, final Mapper<T, ?, X> idMapper);

    /**
     * Persists an object to the repository, returning the index.
     * Where applicable, the object may be updated with the generated index value.
     *
     * @param toInsert The object to insert
     * @return The index, which may have been newly generated
     */
    X insert(final T toInsert);

    /**
     * Deletes a persisted object.
     *
     * @param toDelete The index of the object to delete
     */
    void delete(final X toDelete);

    /**
     * Deletes a persisted object.
     *
     * @param toDelete The object (having the same index as the object) to delete
     */
    void deleteMonadic(final T toDelete);

    void deleteUnsafe(final String whereClause, final X toDelete);

    /**
     * Returns the INSERT query used to insert objects.
     *
     * @return The INSERT query used to insert objects
     */
    String insert();

    /**
     * Returns the mapper used to persist objects.
     *
     * @return The mapper used to persist objects
     */
    InvertibleRowMapper<T> mapper();

    /**
     * Find a mapper for the given table and field name
     * @param table The table name - if null, may be defaulted
     * @param fieldName The field name
     * @param <C> The mapped type
     * @param <I> The internal type
     * @param <O> The external type
     * @return Any such mapper found, or null if none
     */
    <C, I, O> Mapper<C, I, O> findMapper(final String table, final String fieldName);

    /**
     * Finds a mapper for the given field name
     * @param fieldName The field name
     * @param <C> The mapped type
     * @param <I> The internal type
     * @param <O> The external type
     * @return Any such mapper found, or null if none
     */
    <C, I, O> Mapper<C, I, O> findMapper(final String fieldName);

    /**
     * Returns the SELECT query used to retrieve objects.
     *
     * @return The SELECT query used to retrieve objects
     */
    String select();

    /**
     * Retrieves a persisted object.
     *
     * @param withId The object (having the same index as the object) to retrieve
     * @return The persisted object
     */
    Optional<T> getMonadic(final T withId);

    /**
     * Retrieves a persisted object.
     *
     * @param id The index of the object to retrieve
     * @return The persisted object
     */
    Optional<T> get(final X id);

    /**
     * Retrieves all persisted objects having the given index value.
     * This applies primarily to repositories which are
     * not the primary (inserting) repository for a given table.
     *
     * @param id The index value to retrieve for
     * @return A list of persisted objects
     */
    List<T> getFor(final X id);

    /**
     * Retrieves all persisted objects, in index order.
     *
     * @return The list of persisted objects
     */
    List<T> getAll();

    /**
     * Executes arbitrary SQL to retrieve objects. Quite possibly a bad idea.
     *
     * @param sql    SQL which hopefully includes the fields the mapper is looking for
     * @param values The parameters for the given SQL which hopefully is parameterized
     * @return A list of persisted objects
     */
    List<T> getUnsafe(final String sql, final Object... values);
}
