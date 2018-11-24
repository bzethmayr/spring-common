package net.zethmayr.benjamin.spring.common.mapper.base;

/**
 * Some SQL comparison operations.
 */
public enum SqlOp {
    LT("<"),
    LTE("<="),
    GT(">"),
    GTE(">="),
    EQ("="),
    LIKE("LIKE");

    public final String sql;

    SqlOp(final String sql) {
        this.sql = sql;
    }
}
