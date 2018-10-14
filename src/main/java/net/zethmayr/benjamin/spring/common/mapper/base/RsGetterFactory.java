package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Implementations (such as included defaults) can
 * create type-safe getters for {@link ResultSet} fields.
 *
 * @param <O> The JDBC field type
 */
@FunctionalInterface
public interface RsGetterFactory<O> {

    /**
     * Returns a getter for the specified field
     *
     * @param fieldName The field name
     * @return A getter
     */
    RsGetter<O> field(final String fieldName);

    /**
     * Functional interface extrapolated from {@link ResultSet} getX methods.
     *
     * @param <O> The JDBC field type
     */
    @FunctionalInterface
    interface GetterRsFieldName<O> {
        O rsGet(ResultSet rs, String fieldName) throws SQLException;
    }

    /**
     * Generic factory method - creates getters for a given field and type
     *
     * @param rsMethod  The {@link ResultSet} method to use
     * @param fieldName The field name
     * @param <O>       The JDBC field type
     * @return A getter
     */
    static <O> RsGetter<O> factory(final GetterRsFieldName<O> rsMethod, final String fieldName) {
        return (rs) -> {
            try {
                return rsMethod.rsGet(rs, fieldName);
            } catch (SQLException sqle) {
                throw MappingException.because(sqle);
            }
        };
    }

    /**
     * Returns {@link #string(String)} bound as a getter factory.
     *
     * @return A string getter factory
     */
    static RsGetterFactory<String> string() {
        return RsGetterFactory::string;
    }

    /**
     * Specific factory method for
     * String fields.
     *
     * @param fieldName The field name
     * @return A string getter
     */
    static RsGetter<String> string(final String fieldName) {
        return factory(ResultSet::getString, fieldName);
    }

    /**
     * Returns {@link #integer(String)} bound as a getter factory.
     *
     * @return An integer getter factory
     */
    static RsGetterFactory<Integer> integer() {
        return RsGetterFactory::integer;
    }

    /**
     * Specific factory method for
     * Integer fields.
     *
     * @param fieldName The field name
     * @return An integer getter
     */
    static RsGetter<Integer> integer(final String fieldName) {
        return factory(ResultSet::getInt, fieldName);
    }

    /**
     * Returns {@link #longInteger(String)} bound as a getter factory.
     *
     * @return A long getter factory
     */
    static RsGetterFactory<Long> longInteger() {
        return RsGetterFactory::longInteger;
    }

    /**
     * Specific factory method for
     * Long fields.
     *
     * @param fieldName The field name
     * @return A long getter
     */
    static RsGetter<Long> longInteger(final String fieldName) {
        return factory(ResultSet::getLong, fieldName);
    }

    /**
     * Returns {@link #instant(String)} bounds as a getter factory.
     *
     * @return
     */
    static RsGetterFactory<Instant> instant() {
        return RsGetterFactory::instant;
    }

    /**
     * Specific factory method for
     * Instant fields.
     *
     * @param fieldName The field name
     * @return An instant getter
     */
    static RsGetter<Instant> instant(final String fieldName) {
        return factory((rs, f) -> rs.getObject(fieldName, Instant.class), fieldName);
    }
}
