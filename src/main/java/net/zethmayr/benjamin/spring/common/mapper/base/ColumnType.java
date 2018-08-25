package net.zethmayr.benjamin.spring.common.mapper.base;

/**
 * A description of the SQL-level properties of a given field.
 * @param <O>
 */
public interface ColumnType<O> {
    RsGetterFactory<O> getterFactory();

    PsSetterFactory<O> setterFactory();

    String sqlType();

    boolean isIndexColumn();

    Class<O> getExternalClass();

    O limited(O initial);

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
        public Integer limited(Integer in) {
            return in;
        }
    };

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
        public Integer limited(Integer in) {
            return in;
        }
    };

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
        public Long limited(Long in) {
            return in;
        }
    };

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
        public String limited(String in) {
            return in == null ? null : in.length() <= 15 ? in : in.substring(0,15);
        }
    };

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
        public String limited(String in) {
            return in == null ? null : in.length() <= 255 ? in : in.substring(0,255);
        }
    };
}
