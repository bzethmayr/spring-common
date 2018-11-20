package net.zethmayr.benjamin.spring.common.mapper.base;

import lombok.val;
import net.zethmayr.benjamin.spring.common.mapper.HistoryMapper;
import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import org.junit.Test;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.DONT_DELETE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.DONT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.INDEPENDENT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.GTE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MapperAndJoinTest {
    @Test
    public void canBuildAnInstance() {
        val pojoToEnum = MapperAndJoin.<TestPojo, History, Integer>builder()
                .mapper(new HistoryMapper())
                .parentField(TestPojoMapper.EVENT)
                .acceptor(TestPojo::setEvent)
                .getter(single(TestPojo::getEvent))
                .relatedField(HistoryMapper.ID)
                .build();
        assertThat(pojoToEnum.relation(), is(EQ));
        assertThat(pojoToEnum.insertions(), is(DONT_INSERT));
        assertThat(pojoToEnum.deletions(), is(DONT_DELETE));
    }

    @Test
    public void canBuildAFullySpecifiedInstance() {
        val pojoToEnum = MapperAndJoin.<TestPojo, History, Integer>builder()
                .mapper(new HistoryMapper())
                .parentField(TestPojoMapper.EVENT)
                .acceptor(TestPojo::setEvent)
                .getter(single(TestPojo::getEvent))
                .relation(EQ)
                .relatedField(HistoryMapper.ID)
                .insertions(INDEPENDENT_INSERT)
                .deletions(MATERIALIZE_PARENT)
                .build();
        assertThat(pojoToEnum.relation(), is(EQ));
        assertThat(pojoToEnum.insertions(), is(INDEPENDENT_INSERT));
        assertThat(pojoToEnum.deletions(), is(MATERIALIZE_PARENT));
    }

    @Test
    public void canBuildAndCopyAnInstance() {
        val original = MapperAndJoin.<TestPojo, History, Integer>builder()
                .mapper(new HistoryMapper())
                .parentField(TestPojoMapper.EVENT)
                .acceptor(TestPojo::setEvent)
                .relation(GTE)
                .getter(single(TestPojo::getEvent))
                .relatedField(HistoryMapper.ID)
                .build();
        val modified = original.toBuilder()
                .leftIndex(1)
                .build();
        assertThat(original.leftIndex(), is(0));
        assertThat(modified.leftIndex(), is(1));
        assertThat(modified.relation(), is(GTE));
    }
}
