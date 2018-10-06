package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.EnumRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.TestEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestEnumMapper extends EnumRowMapper<TestEnum> {
    public static final Mapper<TestEnum, TestEnum, Integer> ID =
            ComposedMapper.enumId(
                    TestEnum::ordinal,
                    TestEnum::fromOrdinal
            );
    public static final Mapper<TestEnum, String, String> N =
            ComposedMapper.enumSimple(
                    "n",
                    TestEnum::n,
                    ColumnType.SHORT_STRING
            );
    public static final Mapper<TestEnum, Boolean, String> INDICATION =
            ComposedMapper.enumField(
                    "indication",
                    TestEnum::indication,
                    (b) -> Boolean.toString(b),
                    ColumnType.SHORT_STRING,
                    Boolean::valueOf
            );

    public static final List<ClassFieldMapper<TestEnum>> FIELDS = Collections.unmodifiableList(Arrays.asList(
            ID, N, INDICATION
    ));
    public static final String TABLE = "responses";

    public TestEnumMapper() {
        super(
                TestEnum.YES,
                FIELDS,
                TABLE,
                genSelectIds(FIELDS, TABLE),
                genInsert(FIELDS, TABLE)
        );
    }
}
