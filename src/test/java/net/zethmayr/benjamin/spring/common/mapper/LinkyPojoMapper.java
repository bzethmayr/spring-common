package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.LinkyPojo;
import net.zethmayr.benjamin.spring.common.model.TestPojo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.USE_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.DONT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.INDEPENDENT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.adder;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.collection;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

public class LinkyPojoMapper extends JoiningRowMapper<LinkyPojo> {

    public static class CoreMapper extends InvertibleRowMapperBase<LinkyPojo> {
        public static final String TABLE = "linky";
        public static final Mapper<LinkyPojo, Integer, Integer> ID = ComposedMapper.simpleField(
                "id",
                LinkyPojo::getId,
                ColumnType.INTEGER_INDEX,
                LinkyPojo::setId
        );
        public static final Mapper<LinkyPojo, Integer, Integer> LINK = ComposedMapper.simpleField(
                "link",
                LinkyPojo::getLink,
                ColumnType.INTEGER,
                LinkyPojo::setLink
        );
        public static final Mapper<LinkyPojo, String, String> NAME = ComposedMapper.simpleField(
                "name",
                LinkyPojo::getName,
                ColumnType.SHORT_STRING,
                LinkyPojo::setName
        );
        public static final List<ClassFieldMapper<LinkyPojo>> FIELDS = Collections.unmodifiableList(Arrays.asList(
                ID, LINK, NAME
        ));

        public CoreMapper() {
            super(LinkyPojo.class, FIELDS, TABLE, LinkyPojo::new);
        }
    }

    public LinkyPojoMapper() {
        super(new CoreMapper(),
                MapperAndJoin.<LinkyPojo, TestPojo, Integer>builder()
                        .mapper(new TestPojoMapper())
                        .parentField(CoreMapper.ID)
                        .relation(EQ)
                        .relatedField(TestPojoMapper.ID)
                        .acceptor(LinkyPojo::setTop)
                        .getter(single(LinkyPojo::getTop))
                        .insertions(DONT_INSERT)
                        .deletions(USE_PARENT_ID)
                        .build(),
                MapperAndJoin.<LinkyPojo, TestPojo, Integer>builder()
                        .mapper(new TestPojoMapper())
                        .parentField(CoreMapper.LINK)
                        .relation(EQ)
                        .relatedField(TestPojoMapper.STEVE)
                        .acceptor(adder(LinkyPojo::getLeft))
                        .getter(collection(LinkyPojo::getLeft))
                        .insertions(INDEPENDENT_INSERT)
                        .deletions(MATERIALIZE_PARENT)
                        .build()
        );
    }
}
