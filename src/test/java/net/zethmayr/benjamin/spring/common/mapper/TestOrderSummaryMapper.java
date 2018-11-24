package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.TestOrderSummary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestOrderSummaryMapper extends InvertibleRowMapperBase<TestOrderSummary> {
    public static final Mapper<TestOrderSummary, Integer, Integer> ID = ComposedMapper.simpleField(
            "id",
            TestOrderSummary::getId,
            ColumnType.INTEGER_INDEX,
            TestOrderSummary::setId
    );
    public static final Mapper<TestOrderSummary, Integer, Integer> ORDER_ID = ComposedMapper.simpleField(
            "order_id",
            TestOrderSummary::getOrderId,
            ColumnType.INTEGER,
            TestOrderSummary::setOrderId
    );
    public static final Mapper<TestOrderSummary, String, String> SUMMARY = ComposedMapper.simpleField(
            "summary",
            TestOrderSummary::getSummary,
            ColumnType.LONG_STRING,
            TestOrderSummary::setSummary
    );
    public static final List<ClassFieldMapper<TestOrderSummary>> FIELDS = Collections.unmodifiableList(Arrays.asList(
        ID, ORDER_ID, SUMMARY
    ));

    public TestOrderSummaryMapper() {
        super(TestOrderSummary.class, FIELDS, "order_summaries", TestOrderSummary::new);
    }
}
