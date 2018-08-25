package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RsGetterFactory<O> {
    RsGetter<O> field(final String fieldName);

    @FunctionalInterface
    interface GetterRsFieldName<O> {
        O rsGet(ResultSet rs, String fieldName) throws SQLException;
    }

    static <O> RsGetter<O> factory(final GetterRsFieldName<O> rsMethod, final String fieldName) {
        return (rs) -> {
            try {
                return rsMethod.rsGet(rs, fieldName);
            } catch (SQLException sqle) {
                throw MappingException.because(sqle);
            }
        };
    }

    static RsGetterFactory<String> string() {
        return RsGetterFactory::string;
    }
    static RsGetter<String> string(final String fieldName) {
        return factory(ResultSet::getString, fieldName);
    }

    static RsGetterFactory<Integer> integer() {
        return RsGetterFactory::integer;
    }
    static RsGetter<Integer> integer(final String fieldName) {
        return factory(ResultSet::getInt, fieldName);
    }

    static RsGetterFactory<Long> longInteger() {
        return RsGetterFactory::longInteger;
    }
    static RsGetter<Long> longInteger(final String fieldName) {
        return factory(ResultSet::getLong, fieldName);
    }
}
