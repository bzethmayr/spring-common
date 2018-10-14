package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

/**
 * Implementations (such as included defaults) can
 * create type-safe value setters for {@link PreparedStatement} fields.
 *
 * @param <O> The JDBC type of values set
 */
@FunctionalInterface
public interface PsSetterFactory<O> {

    /**
     * Returns a setter for the specified ordinal.
     *
     * @param ordinal The insert ordinal
     * @return A setter
     */
    PsSetter<O> getForInsert(final int ordinal);

    /**
     * Functional interface extracted from various {@link PreparedStatement} setX methods.
     *
     * @param <O> The JDBC field type
     */
    @FunctionalInterface
    interface SetterPsOrdinalValue<O> {
        void psSet(final PreparedStatement ps, final int ordinal, final O value) throws SQLException;
    }

    /**
     * Generic factory method - creates setters for a given ordinal and type
     *
     * @param psMethod The {@link PreparedStatement} method to use
     * @param ordinal  The insert ordinal
     * @param sqlType  The SQL type information
     * @param <O>      The JDBC field type
     * @return A setter
     */
    static <O> PsSetter<O> factory(SetterPsOrdinalValue<O> psMethod, final int ordinal, final int sqlType) {
        return (ps, value) -> {
            try {
                if (value != null) {
                    psMethod.psSet(ps, ordinal, value);
                } else {
                    ps.setNull(ordinal, sqlType);
                }
            } catch (SQLException sqle) {
                throw MappingException.because(sqle);
            }
        };
    }

    /**
     * Specific factory method for setters for fixed-length strings.
     *
     * @param ordinal The insert ordinal
     * @return A short string setter
     */
    static PsSetter<String> shortString(final int ordinal) {
        return factory(PreparedStatement::setString, ordinal, Types.CHAR);
    }

    /**
     * Specific factory method for setters for variable-length strings.
     *
     * @param ordinal The insert ordinal
     * @return A long string setter
     */
    static PsSetter<String> longString(final int ordinal) {
        return factory(PreparedStatement::setString, ordinal, Types.VARCHAR);
    }

    /**
     * Specific factory method for setters for integers.
     *
     * @param ordinal The insert ordinal.
     * @return An integer setter
     */
    static PsSetter<Integer> integer(final int ordinal) {
        return factory(PreparedStatement::setInt, ordinal, Types.INTEGER);
    }

    /**
     * Specific factory method for setters for longs.
     *
     * @param ordinal The insert ordinal
     * @return A long setter
     */
    static PsSetter<Long> longInteger(final int ordinal) {
        return factory(PreparedStatement::setLong, ordinal, Types.BIGINT);
    }

    /**
     * Specific factory method for setters for instants (timestamps).
     *
     * @param ordinal The insert ordinal
     * @return An instant setter
     */
    static PsSetter<Instant> instant(final int ordinal) {
        return factory((ps, i, o) -> ps.setObject(i, o, Types.TIMESTAMP_WITH_TIMEZONE), ordinal, Types.TIMESTAMP_WITH_TIMEZONE);
    }
}
