package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TestPojoMapper extends InvertibleRowMapperBase<TestPojo> {
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
    public static final Mapper<TestPojo, BigDecimal, Long> WEIGHTING = ComposedMapper.money(
            "weighting",
            TestPojo::getWeighting,
            TestPojo::setWeighting
    );
    public static final List<ClassFieldMapper<TestPojo>> FIELDS = Arrays.asList(
            ID, EVENT, COMMENT, STEVE, WEIGHTING
    );
    public static final String TABLE = "commentary";

    @SuppressWarnings("deprecation") // Uses deprecated constructor - as long as we keep it we need to not break it.
    public TestPojoMapper() {
        super(TestPojo.class, FIELDS, TABLE, genSelect(FIELDS, TABLE), genInsert(FIELDS, TABLE));
    }
}
