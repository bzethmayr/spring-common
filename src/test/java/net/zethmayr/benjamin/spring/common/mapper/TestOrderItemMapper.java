package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.TestItem;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.model.TestOrderItem;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

public class TestOrderItemMapper extends JoiningRowMapper<TestOrderItem> {
    public TestOrderItemMapper() {
        super(new CoreMapper(),
                MapperAndJoin.<TestOrderItem, TestItem, Integer>builder()
                        .mapper(new TestItemMapper())
                        .parentField(CoreMapper.ITEM_ID)
                        .getter(single(TestOrderItem::getItem))
                        .acceptor(TestOrderItem::setItem)
                        .relation(EQ)
                        .relatedField(TestItemMapper.ID)
                        .insertions(MapperAndJoin.InsertStyle.PARENT_NEEDS_ID) // no, you wouldn't really, though.
                        .deletions(MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT)
                        .build()
        );
    }

    public static class CoreMapper extends InvertibleRowMapperBase<TestOrderItem> {
        public static final Mapper<TestOrderItem, Integer, Integer> ID = ComposedMapper.simpleField(
                "id",
                TestOrderItem::getId,
                ColumnType.INTEGER_INDEX,
                TestOrderItem::setId
        );
        public static final Mapper<TestOrderItem, Integer, Integer> ORDER_ID = ComposedMapper.simpleField(
                "order_id",
                TestOrderItem::getOrderId,
                ColumnType.INTEGER,
                TestOrderItem::setOrderId
        );
        public static final Mapper<TestOrderItem, Integer, Integer> ITEM_ID = ComposedMapper.simpleField(
                "item_id",
                TestOrderItem::getItemId,
                ColumnType.INTEGER,
                TestOrderItem::setItemId
        );
        public static final Mapper<TestOrderItem, Integer, Integer> QUANTITY = ComposedMapper.simpleField(
                "quantity",
                TestOrderItem::getQuantity,
                ColumnType.INTEGER,
                TestOrderItem::setQuantity
        );
        public static final List<ClassFieldMapper<TestOrderItem>> FIELDS = Collections.unmodifiableList(Arrays.asList(
                ID, ORDER_ID, ITEM_ID, QUANTITY
        ));
        public static final String TABLE = "order_items";

        public CoreMapper() {
            super(TestOrderItem.class, FIELDS, TABLE, TestOrderItem::new);
        }
    }
}
