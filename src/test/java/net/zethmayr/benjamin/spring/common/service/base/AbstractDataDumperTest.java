package net.zethmayr.benjamin.spring.common.service.base;

import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.TestPojoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.COMMENT;
import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.ID;
import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.STEVE;
import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.WEIGHTING;
import static net.zethmayr.benjamin.spring.common.model.History.MAGNA_CARTA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDataDumperTest {

    @Mock
    TestPojoRepository pojoRepository;

    private final AbstractDataDumper<TestPojo> underTest = new AbstractDataDumper<TestPojo>() {
        @Override
        protected List<DumpExtractor<TestPojo>> constructExtractors() {
            return Arrays.asList(
                    stringableExtractor(ID),
                    directExtractor(COMMENT),
                    new DumpExtractor<>("event name",
                            e -> e.getEvent().name(),
                            Function.identity()
                    ),
                    listValueExtractor("prior event",
                            e -> Collections.singletonList(e.getEvent().getPriorRelated()),
                            e -> true,
                            Enum::name
                    ),
                    setValueExtractor("descends from magna carta?",
                            e -> {
                                final History prior = e.getEvent().getPriorRelated();
                                if (prior == null) {
                                    return EnumSet.noneOf(History.class);
                                } else {
                                    return EnumSet.of(prior);
                                }
                            },
                            MAGNA_CARTA
                            ),
                    new DumpExtractor<TestPojo>("years",
                            e -> e.getEvent().year(),
                            NULL_IS_NULL_IS_NULL_IS_EMPTY).aggregator(numericAggregator()),
                    stringableExtractor(WEIGHTING),
                    stringableExtractor(STEVE).aggregator(numericAggregator())
            );
        }

        @Override
        protected boolean aggregate() {
            return true;
        }
    };

    @Before
    public void setUp() {
        when(pojoRepository.select()).thenReturn("");
    }

    @Test
    public void canDumpNothing() {
        underTest.dump(null, pojoRepository);
    }
}
