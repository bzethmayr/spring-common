package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.instanceGetter;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.instanceState;

public class TestJoiningMapper extends JoiningRowMapper<TestPojo> {
    public TestJoiningMapper() {
        super(new TestPojoMapper(),
                MapperAndJoin.<TestPojo, History>builder()
                        .getterStateFactory(instanceState(TestPojo::getEvent))
                        .parentGetter(instanceGetter())
                        .mapper(new HistoryMapper())
                        .parentAcceptor(TestPojo::setEvent)
                        .build()
        );
    }
}
