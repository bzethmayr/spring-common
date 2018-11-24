package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.model.TestUser;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.USE_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.NEEDS_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.collection;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

/**
 * This mapper is of recursion depth > 1.
 */
public class TestUserOrdersMapper extends JoiningRowMapper<TestUser> {
    public TestUserOrdersMapper() {
        super(new TestUserMapper(),
                MapperAndJoin.<TestUser, TestOrder, Integer>builder()
                        .mapper(new TestOrderMapper())
                        .parentField(TestUserMapper.ID)
                        .acceptor((u, o) -> u.getOrders().add(o))
                        .getter(collection(TestUser::getOrders))
                        .relation(EQ)
                        .relatedField(TestOrderMapper.CoreMapper.USER_ID)
                        .deletions(USE_PARENT_ID)
                        .insertions(NEEDS_PARENT_ID)
                        .build());
    }
}
