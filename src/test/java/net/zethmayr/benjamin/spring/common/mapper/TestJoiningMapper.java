package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.DONT_DELETE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.DONT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

public class TestJoiningMapper extends JoiningRowMapper<TestPojo> {
    public TestJoiningMapper() {
        super(new TestPojoMapper(),
                MapperAndJoin.<TestPojo, History, Integer>builder()
                        .mapper(new HistoryMapper())
                        .parentField(TestPojoMapper.EVENT)
                        .relation(EQ)
                        .relatedField(HistoryMapper.ID)
                        .getter(single(TestPojo::getEvent))
                        .acceptor(TestPojo::setEvent)
                        .insertions(DONT_INSERT)
                        .deletions(DONT_DELETE)
                        .build()
        );
    }
}
