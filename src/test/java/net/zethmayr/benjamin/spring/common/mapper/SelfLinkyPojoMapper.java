package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.SelfLinkyPojo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.zethmayr.benjamin.spring.common.mapper.SelfLinkyPojoMapper.CoreMapper.GROUP;
import static net.zethmayr.benjamin.spring.common.mapper.SelfLinkyPojoMapper.CoreMapper.ID;
import static net.zethmayr.benjamin.spring.common.mapper.SelfLinkyPojoMapper.CoreMapper.OWNS;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.DONT_DELETE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.INDEPENDENT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.NEEDS_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.PARENT_NEEDS_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.collection;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;

public class SelfLinkyPojoMapper extends JoiningRowMapper<SelfLinkyPojo> {
    public static class CoreMapper extends InvertibleRowMapperBase<SelfLinkyPojo> {
        public static final Mapper<SelfLinkyPojo, Integer, Integer> ID = ComposedMapper.simpleField(
                "id",
                SelfLinkyPojo::getId,
                ColumnType.INTEGER_INDEX,
                SelfLinkyPojo::setId
        );
        public static final Mapper<SelfLinkyPojo, String, String> NAME = ComposedMapper.simpleField(
                "name",
                SelfLinkyPojo::getName,
                ColumnType.SHORT_STRING,
                SelfLinkyPojo::setName
        );
        public static final Mapper<SelfLinkyPojo, Integer, Integer> GROUP = ComposedMapper.simpleField(
                "gorp",
                SelfLinkyPojo::getGroup,
                ColumnType.INTEGER,
                SelfLinkyPojo::setGroup
        );
        public static final Mapper<SelfLinkyPojo, Integer, Integer> OWNS = ComposedMapper.simpleField(
                "owns",
                SelfLinkyPojo::getOwns,
                ColumnType.INTEGER,
                SelfLinkyPojo::setOwns
        );
        public static final List<ClassFieldMapper<SelfLinkyPojo>> FIELDS = Collections.unmodifiableList(Arrays.asList(
                ID, NAME, GROUP, OWNS
        ));

        public CoreMapper() {
            super(SelfLinkyPojo.class, FIELDS, "self_linky", SelfLinkyPojo::new);
        }
    }

    public SelfLinkyPojoMapper() {
        super(new CoreMapper(),
                MapperAndJoin.<SelfLinkyPojo, SelfLinkyPojo, Integer>builder()
                        .mapper(new CoreMapper())
                        .parentField(GROUP)
                        .relation(EQ)
                        .relatedField(GROUP)
                        .deletions(MATERIALIZE_PARENT)
                        .insertions(INDEPENDENT_INSERT)
                        .acceptor((p, o) -> p.getNeighbors().add(o))
                        .getter(collection(SelfLinkyPojo::getNeighbors))
                        .build(),
                MapperAndJoin.<SelfLinkyPojo, SelfLinkyPojo, Integer>builder()
                        .mapper(new CoreMapper())
                        .parentField(OWNS)
                        .relation(EQ)
                        .relatedField(ID)
                        .deletions(MATERIALIZE_PARENT)
                        .insertions(PARENT_NEEDS_ID)
                        .acceptor(SelfLinkyPojo::setOwned)
                        .getter(single(SelfLinkyPojo::getOwned))
                        .build(),
                MapperAndJoin.<SelfLinkyPojo, SelfLinkyPojo, Integer>builder()
                        .mapper(new CoreMapper())
                        .parentField(ID)
                        .relation(EQ)
                        .relatedField(OWNS)
                        .deletions(DONT_DELETE)
                        .insertions(NEEDS_PARENT_ID)
                        .acceptor((p, o) -> p.getOwners().add(o))
                        .getter(collection(SelfLinkyPojo::getOwners))
                        .build()
        );
    }
}
