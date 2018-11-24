package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.TestItem;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestItemMapper extends InvertibleRowMapperBase<TestItem> {
    public static final Mapper<TestItem, Integer, Integer> ID = ComposedMapper.simpleField(
            "id",
            TestItem::getId,
            ColumnType.INTEGER_INDEX,
            TestItem::setId
    );
    public static final Mapper<TestItem, String, String> NAME = ComposedMapper.simpleField(
            "item_name",
            TestItem::getName,
            ColumnType.LONG_STRING,
            TestItem::setName
    );
    public static final Mapper<TestItem, BigDecimal, Long> PRICE = ComposedMapper.money(
            "price",
            TestItem::getPrice,
            TestItem::setPrice
    );
    public static final List<ClassFieldMapper<TestItem>> FIELDS = Collections.unmodifiableList(Arrays.asList(
        ID, NAME, PRICE
    ));
    public static final String TABLE = "items";
    public TestItemMapper() {
        super(TestItem.class, FIELDS, TABLE, TestItem::new);
    }
}
