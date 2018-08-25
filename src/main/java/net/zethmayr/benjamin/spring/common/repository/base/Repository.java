package net.zethmayr.benjamin.spring.common.repository.base;

import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;

import java.util.List;
import java.util.Optional;

public interface Repository<T, X> {
    X insert(final T toInsert);
    void delete(final X toDelete);
    void deleteMonadic(final T toDelete);
    String insert();
    InvertibleRowMapper<T> mapper();
    String select();
    Optional<T> getMonadic(final T withId);
    Optional<T> get(final X id);
    List<T> getFor(final X id);
    List<T> getAll();
    List<T> getUnsafe(final String sql, final Object... values);
}
