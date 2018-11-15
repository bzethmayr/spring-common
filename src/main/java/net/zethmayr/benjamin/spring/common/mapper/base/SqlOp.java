package net.zethmayr.benjamin.spring.common.mapper.base;

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
