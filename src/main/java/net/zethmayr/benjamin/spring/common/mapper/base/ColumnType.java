package net.zethmayr.benjamin.spring.common.mapper.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

/**
 * A description of the SQL-level properties of a given field.
 *
 * @param <O> The JDBC-level type of the field
 */
public interface ColumnType<O> {
    /**
     * Provides access to the {@link ResultSet} getter factory.
     *
     * @return the getter factory
     */
    RsGetterFactory<O> getterFactory();

    /**
     * Provides access to the {@link PreparedStatement} setter factory.
     *
     * @return the setter factory
     */
    PsSetterFactory<O> setterFactory();

    /**
     * Returns the SQL used to declare a field of this type.
     *
     * @return the SQL used to declare a field of this type.
     */
    String sqlType();

    /**
     * Returns true if the field is a generated index, else false.
     *
     * @return true if the field is a generated index, else false.
     */
    boolean isIndexColumn();

    /**
     * Returns the external (JDBC-level) class of the field
     *
     * @return the external (JDBC-level) class of the field
     */
    Class<O> getExternalClass();

    /**
     * Enforces limits on values about to be stored.
     *
     * @param initial The value presented for storage
     * @return The limited value
     */
    O limited(O initial);

    /**
     * A generated 32-bit signed integer index column.
     */
    ColumnType<Integer> INTEGER_INDEX = new ColumnType<Integer>() {
        @Override
        public RsGetterFactory<Integer> getterFactory() {
            return RsGetterFactory.integer();
        }

        @Override
        public PsSetterFactory<Integer> setterFactory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String sqlType() {
            return "INTEGER AUTO_INCREMENT PRIMARY KEY";
        }

        @Override
        public boolean isIndexColumn() {
            return true;
        }

        @Override
        public Class<Integer> getExternalClass() {
            return Integer.class;
        }

        @Override
        public Integer limited(final Integer in) {
            return in;
        }
    };

    /**
     * A signed 32-bit integer column.
     */
    ColumnType<Integer> INTEGER = new ColumnType<Integer>() {
        @Override
        public RsGetterFactory<Integer> getterFactory() {
            return RsGetterFactory.integer();
        }

        @Override
        public PsSetterFactory<Integer> setterFactory() {
            return PsSetterFactory::integer;
        }

        @Override
        public String sqlType() {
            return "INTEGER";
        }

        @Override
        public boolean isIndexColumn() {
            return false;
        }

        @Override
        public Class<Integer> getExternalClass() {
            return Integer.class;
        }

        @Override
        public Integer limited(final Integer in) {
            return in;
        }
    };

    /**
     * A signed 64-bit integer column.
     */
    ColumnType<Long> LONG = new ColumnType<Long>() {
        @Override
        public RsGetterFactory<Long> getterFactory() {
            return RsGetterFactory.longInteger();
        }

        @Override
        public PsSetterFactory<Long> setterFactory() {
            return PsSetterFactory::longInteger;
        }

        @Override
        public String sqlType() {
            return "BIGINT";
        }

        @Override
        public boolean isIndexColumn() {
            return false;
        }

        @Override
        public Class<Long> getExternalClass() {
            return Long.class;
        }

        @Override
        public Long limited(final Long in) {
            return in;
        }
    };

    /**
     * A short fixed-length string column
     * which truncates excess data on write.
     */
    ColumnType<String> SHORT_STRING = new ColumnType<String>() {
        @Override
        public RsGetterFactory<String> getterFactory() {
            return RsGetterFactory.string();
        }

        @Override
        public PsSetterFactory<String> setterFactory() {
            return PsSetterFactory::shortString;
        }

        @Override
        public String sqlType() {
            return "CHAR(15)";
        }

        @Override
        public boolean isIndexColumn() {
            return false;
        }

        @Override
        public Class<String> getExternalClass() {
            return String.class;
        }

        @Override
        public String limited(final String in) {
            return in == null ? null : in.length() <= 15 ? in : in.substring(0, 15);
        }
    };

    /**
     * A longish variable-length string column
     * which truncates excess data on write.
     */
    ColumnType<String> LONG_STRING = new ColumnType<String>() {
        @Override
        public RsGetterFactory<String> getterFactory() {
            return RsGetterFactory.string();
        }

        @Override
        public PsSetterFactory<String> setterFactory() {
            return PsSetterFactory::longString;
        }

        @Override
        public String sqlType() {
            return "VARCHAR(255)";
        }

        @Override
        public boolean isIndexColumn() {
            return false;
        }

        @Override
        public Class<String> getExternalClass() {
            return String.class;
        }

        @Override
        public String limited(final String in) {
            return in == null ? null : in.length() <= 255 ? in : in.substring(0, 255);
        }
    };

    /**
     * A column type for dates.
     */
    ColumnType<Instant> INSTANT = new ColumnType<Instant>() {
        @Override
        public RsGetterFactory<Instant> getterFactory() {
            return RsGetterFactory.instant();
        }

        @Override
        public PsSetterFactory<Instant> setterFactory() {
            return PsSetterFactory::instant;
        }

        @Override
        public String sqlType() {
            return "TIMESTAMP WITH TIME ZONE";
        }

        @Override
        public boolean isIndexColumn() {
            return false;
        }

        @Override
        public Class<Instant> getExternalClass() {
            return Instant.class;
        }

        @Override
        public Instant limited(final Instant initial) {
            return initial;
        }
    };
}
