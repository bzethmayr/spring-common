package net.zethmayr.benjamin.spring.common.repository.base;

import com.sun.org.apache.xpath.internal.Arg;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.FieldMapperTransform;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin;
import net.zethmayr.benjamin.spring.common.model.SelfLinkyPojo;
import net.zethmayr.benjamin.spring.common.util.ListBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.DONT_DELETE;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.DeleteStyle.MATERIALIZE_PARENT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.INDEPENDENT_INSERT;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.NEEDS_PARENT_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.InsertStyle.PARENT_NEEDS_ID;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.collection;
import static net.zethmayr.benjamin.spring.common.mapper.base.MapperAndJoin.single;
import static net.zethmayr.benjamin.spring.common.mapper.base.SqlOp.EQ;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class JoiningRepositorySpyTest {

    @SpyBean
    JdbcTemplate jdbcTemplate;

    private JoiningRepository<SelfLinkyPojo, Integer> underTest;

    // Spies
    private InvertibleRowMapperBase<SelfLinkyPojo> coreMapper;
    private JoiningRowMapper<SelfLinkyPojo> joiningMapper;
    private MapperRepository<SelfLinkyPojo, Integer> coreRepository;

    private Mapper<SelfLinkyPojo, Integer, Integer> ID;
    private Mapper<SelfLinkyPojo, String, String> NAME;
    private Mapper<SelfLinkyPojo, Integer, Integer> GROUP;
    private Mapper<SelfLinkyPojo, Integer, Integer> OWNS;
    private MapperAndJoin<SelfLinkyPojo, SelfLinkyPojo, Integer> groupJoin;
    private MapperAndJoin<SelfLinkyPojo, SelfLinkyPojo, Integer> ownedJoin;
    private MapperAndJoin<SelfLinkyPojo, SelfLinkyPojo, Integer> ownersJoin;

    private List<?> spies;

    @Before
    public void setUp() {
        constructWithInjectedSpies();
        spies.forEach(Mockito::reset);
    }

    private void constructWithInjectedSpies() {
        val spiesBuilder = ListBuilder.<Object>array(
                this.ID = spy(ComposedMapper.simpleField(
                        "id",
                        SelfLinkyPojo::getId,
                        ColumnType.INTEGER_INDEX,
                        SelfLinkyPojo::setId
                )),
                this.NAME = spy(ComposedMapper.simpleField(
                        "name",
                        SelfLinkyPojo::getName,
                        ColumnType.SHORT_STRING,
                        SelfLinkyPojo::setName
                )),
                this.GROUP = spy(ComposedMapper.simpleField(
                        "gorp",
                        SelfLinkyPojo::getGroup,
                        ColumnType.INTEGER,
                        SelfLinkyPojo::setGroup
                )),
                this.OWNS = spy(ComposedMapper.simpleField(
                        "owns",
                        SelfLinkyPojo::getOwns,
                        ColumnType.INTEGER,
                        SelfLinkyPojo::setOwns
                )));
        final List<ClassFieldMapper<SelfLinkyPojo>> FIELDS = Collections.unmodifiableList(Arrays.asList(
                ID, NAME, GROUP, OWNS
        ));
        spies = spiesBuilder.add(
                this.coreMapper = spy(new InvertibleRowMapperBase<SelfLinkyPojo>(SelfLinkyPojo.class, FIELDS, "self_linky", SelfLinkyPojo::new) {
                }),
                this.coreRepository = spy(new MapperRepository<SelfLinkyPojo, Integer>(jdbcTemplate, coreMapper, ID) {
                }),

                this.groupJoin = spy(MapperAndJoin.<SelfLinkyPojo, SelfLinkyPojo, Integer>builder()
                        .mapper(coreMapper)
                        .parentField(GROUP)
                        .relation(EQ)
                        .relatedField(GROUP)
                        .deletions(MATERIALIZE_PARENT)
                        .insertions(INDEPENDENT_INSERT)
                        .acceptor((p, o) -> p.getNeighbors().add(o))
                        .getter(collection(SelfLinkyPojo::getNeighbors))
                        .build()),
                this.ownedJoin = spy(MapperAndJoin.<SelfLinkyPojo, SelfLinkyPojo, Integer>builder()
                        .mapper(coreMapper)
                        .parentField(OWNS)
                        .relation(EQ)
                        .relatedField(ID)
                        .deletions(MATERIALIZE_PARENT)
                        .insertions(PARENT_NEEDS_ID)
                        .acceptor(SelfLinkyPojo::setOwned)
                        .getter(single(SelfLinkyPojo::getOwned))
                        .build()),
                this.ownersJoin = spy(MapperAndJoin.<SelfLinkyPojo, SelfLinkyPojo, Integer>builder()
                        .mapper(coreMapper)
                        .parentField(ID)
                        .relation(EQ)
                        .relatedField(OWNS)
                        .deletions(DONT_DELETE)
                        .insertions(NEEDS_PARENT_ID)
                        .acceptor((p, o) -> p.getOwners().add(o))
                        .getter(collection(SelfLinkyPojo::getOwners))
                        .build()),
                this.joiningMapper = spy(new JoiningRowMapper<SelfLinkyPojo>(coreMapper,
                        groupJoin,
                        ownedJoin,
                        ownersJoin
                ) {
                }))
                .build();
        this.underTest = new JoiningRepository<SelfLinkyPojo, Integer>(jdbcTemplate, joiningMapper, coreRepository) {
        };
    }

    private ArgumentMatcher<FieldMapperTransform> transformerInspector(final String fieldName) {
        return (t) -> {
            LOG.info("Saw {}{}", t.fieldName(fieldName), t.joinIndex());
            return FieldMapperTransform.class.isAssignableFrom(t.getClass());
        };
    }

    @Test
    public void rebindsAllFields() {
        constructWithInjectedSpies();
        verify(ID, atLeastOnce()).copyTransforming(argThat(transformerInspector(ID.fieldName)));
        verify(NAME, atLeastOnce()).copyTransforming(argThat(transformerInspector(NAME.fieldName)));
        verify(GROUP, atLeastOnce()).copyTransforming(argThat(transformerInspector(GROUP.fieldName)));
        verify(OWNS, atLeastOnce()).copyTransforming(argThat(transformerInspector(OWNS.fieldName)));
    }
}
