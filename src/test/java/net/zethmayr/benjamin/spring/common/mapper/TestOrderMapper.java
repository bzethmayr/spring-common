package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.model.TestOrderItem;
import net.zethmayr.benjamin.spring.common.model.TestOrderSummary;
import net.zethmayr.benjamin.spring.common.model.TestUser;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.collection;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

public class TestOrderMapper extends JoiningRowMapper<TestOrder> {

    public static class CoreMapper extends InvertibleRowMapperBase<TestOrder> {
        public static final Mapper<TestOrder, Integer, Integer> ID = ComposedMapper.simpleField(
                "id",
                TestOrder::getId,
                ColumnType.INTEGER_INDEX,
                TestOrder::setId
        );
        public static final Mapper<TestOrder, Instant, Long> ORDERED_AT = ComposedMapper.field(
                "ordered_at",
                TestOrder::getOrderedAt,
                Instant::toEpochMilli,
                ColumnType.LONG,
                Instant::ofEpochMilli,
                TestOrder::setOrderedAt
        );
        public static final Mapper<TestOrder, Integer, Integer> USER_ID = ComposedMapper.simpleField(
                "user_id",
                TestOrder::getUserId,
                ColumnType.INTEGER,
                TestOrder::setUserId
        );
        public static final List<ClassFieldMapper<TestOrder>> FIELDS = Collections.unmodifiableList(Arrays.asList(
                ID, ORDERED_AT, USER_ID
        ));
        public static final String TABLE = "orders";

        public CoreMapper() {
            super(TestOrder.class, FIELDS, TABLE);
        }

        @Override
        public TestOrder empty() {
            return new TestOrder();
        }
    }

    public TestOrderMapper() {
        super(new CoreMapper(),
                MapperAndJoin.<TestOrder, TestUser, Integer>builder()
                        .mapper(new TestUserMapper())
                        .parentField(CoreMapper.USER_ID)
                        .getter(single(TestOrder::getUser))
                        .acceptor(TestOrder::setUser)
                        .relation(EQ)
                        .relatedField(TestUserMapper.ID)
                        .insertions(MapperAndJoin.InsertStyle.PARENT_NEEDS_ID) // but really, don't insert
                        .deletions(MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT) // but really, don't delete
                        .build(),
                MapperAndJoin.<TestOrder, TestOrderItem, Integer>builder()
                        .mapper(new TestOrderItemMapper())
                        .parentField(CoreMapper.ID)
                        .getter(collection(TestOrder::getItems))
                        .acceptor((o, i) -> o.getItems().add(i))
                        .relation(EQ)
                        .relatedField(TestOrderItemMapper.CoreMapper.ORDER_ID)
                        .insertions(MapperAndJoin.InsertStyle.NEEDS_PARENT_ID)
                        .deletions(MapperAndJoin.DeleteStyle.USE_PARENT_ID)
                        .build(),
                MapperAndJoin.<TestOrder, TestOrderSummary, Integer>builder()
                        .mapper(new TestOrderSummaryMapper())
                        .parentField(CoreMapper.ID)
                        .getter(single(TestOrder::getSummary))
                        .acceptor(TestOrder::setSummary)
                        .relation(EQ)
                        .relatedField(TestOrderSummaryMapper.ORDER_ID)
                        .insertions(MapperAndJoin.InsertStyle.NEEDS_PARENT_ID)
                        .deletions(MapperAndJoin.DeleteStyle.USE_PARENT_ID)
                        .build()
        );
    }

}
