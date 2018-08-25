package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Factory to create {@link PsSetter} lambdas.
 * @param <O>
 */
@FunctionalInterface
public interface PsSetterFactory<O> {

    PsSetter<O> getForInsert(final int ordinal);

    @FunctionalInterface
    interface SetterPsOrdinalValue<O> {
        void psSet(final PreparedStatement ps, final int ordinal, final O value) throws SQLException;
    }

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

    static PsSetter<String> shortString(final int ordinal) {
        return factory(PreparedStatement::setString, ordinal, Types.CHAR);
    }

    static PsSetter<String> longString(final int ordinal) {
        return factory(PreparedStatement::setString, ordinal, Types.VARCHAR);
    }

    static PsSetter<Integer> integer(final int ordinal) {
        return factory(PreparedStatement::setInt, ordinal, Types.INTEGER);
    }

    static PsSetter<Long> longInteger(final int ordinal) {
        return factory(PreparedStatement::setLong, ordinal, Types.BIGINT);
    }
}
