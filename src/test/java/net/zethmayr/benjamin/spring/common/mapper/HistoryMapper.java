package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.History;

import java.util.Arrays;
import java.util.List;

import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.INSTANT;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.INTEGER;
import static net.zethmayr.benjamin.spring.common.mapper.base.ColumnType.LONG_STRING;

public class HistoryMapper extends EnumRowMapper<History> {
    public static final Mapper<History, History, Integer> ID = ComposedMapper.enumId(
            History::ordinal,
            History::fromOrdinal
    );

    public static final List<ClassFieldMapper<History>> FIELDS = Arrays.asList(
            ID,
            ComposedMapper.enumSimple("name", History::name, LONG_STRING),
            ComposedMapper.enumSimple("when", History::when, INSTANT),
            ComposedMapper.enumField("prior_related", History::getPriorRelated, History::ordinal, INTEGER, History::fromOrdinal)
    );

    public static final String TABLE = "history";

    public HistoryMapper() {
        super(History.COLUMBUS, FIELDS, TABLE);
    }
}
