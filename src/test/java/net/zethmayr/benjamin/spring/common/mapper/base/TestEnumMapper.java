package net.zethmayr.benjamin.spring.common.mapper.base;

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

    public static final String TABLE = "responses";
    public static final List<ClassFieldMapper<TestEnum>> FIELDS = Collections.unmodifiableList(Arrays.asList(
            ID,
            ComposedMapper.enumDirect(
                    "n",
                    TestEnum::n,
                    ColumnType.SHORT_STRING
            ),
            ComposedMapper.enumField(
                    "indication",
                    TestEnum::indication,
                    (b) -> Boolean.toString(b),
                    ColumnType.SHORT_STRING,
                    Boolean::valueOf
            )
    ));

    public TestEnumMapper() {
        super(
                TestEnum.class,
                FIELDS,
                TABLE,
                genSelectIds(FIELDS, TABLE),
                genInsert(FIELDS, TABLE)
        );
    }
}
