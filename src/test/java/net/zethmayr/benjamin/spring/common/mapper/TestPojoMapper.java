package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.DesMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.SerMapper;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TestPojoMapper extends InvertibleRowMapper<TestPojo> {
    public static final Mapper<TestPojo, Integer, Integer> ID = ComposedMapper.simpleField(
            "id",
            TestPojo::getId,
            ColumnType.INTEGER_INDEX,
            TestPojo::setId
    );
    public static final Mapper<TestPojo, History, Integer> EVENT = ComposedMapper.field(
            "event",
            TestPojo::getEvent,
            History::ordinal,
            ColumnType.INTEGER,
            History::fromOrdinal,
            TestPojo::setEvent
    );
    public static final Mapper<TestPojo, String, String> COMMENT = ComposedMapper.simpleField(
            "comment",
            TestPojo::getComment,
            ColumnType.LONG_STRING,
            TestPojo::setComment
    );
    public static final Mapper<TestPojo, Integer, Integer> STEVE = ComposedMapper.simpleField(
            "steve",
            TestPojo::getSteve,
            ColumnType.INTEGER,
            TestPojo::setSteve
    );
    public static final Mapper<TestPojo, BigDecimal, String> WEIGHTING = ComposedMapper.field(
            "weighting",
            TestPojo::getWeighting,
            SerMapper.MONEY,
            ColumnType.SHORT_STRING,
            DesMapper.MONEY,
            TestPojo::setWeighting
    );
    public static final List<ClassFieldMapper<TestPojo>> FIELDS = Arrays.asList(
            ID, EVENT, COMMENT, STEVE, WEIGHTING
    );
    public static final String TABLE = "commentary";
    public TestPojoMapper() {
        super(TestPojo.class, FIELDS,TABLE, genSelect(FIELDS, TABLE), genInsert(FIELDS, TABLE));
    }
    @Override
    public TestPojo empty() {
        return new TestPojo();
    }
}
