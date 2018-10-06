package net.zethmayr.benjamin.spring.common.service.base;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.TestPojoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.COMMENT;
import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.ID;
import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.STEVE;
import static net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper.WEIGHTING;
import static net.zethmayr.benjamin.spring.common.model.History.COLUMBUS;
import static net.zethmayr.benjamin.spring.common.model.History.DECLARATION_OF_INDEPENDENCE;
import static net.zethmayr.benjamin.spring.common.model.History.MAGNA_CARTA;
import static net.zethmayr.benjamin.spring.common.service.base.AbstractDataDumper.SqlOp.GT;
import static net.zethmayr.benjamin.spring.common.service.base.AbstractDataDumper.SqlOp.LT;
import static net.zethmayr.benjamin.spring.common.service.base.AbstractDataDumper.filter;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class AbstractDataDumperTest {

    @Mock
    TestPojoRepository pojoRepository;

    private final AbstractDataDumper<TestPojo> underTest = new AbstractDataDumper<TestPojo>() {
        @Override
        protected List<DumpExtractor<TestPojo>> constructExtractors() {
            return Arrays.asList(
                    stringableExtractor(ID),
                    stringExtractor(COMMENT),
                    stringExtractor(COMMENT, QUOTED),
                    stringExtractor(COMMENT, QUOTE_IF_NEEDED),
                    new DumpExtractor<>("event name",
                            e -> {
                                final History event = e.getEvent();
                                return event == null ? null : event.name();
                            },
                            NULL_IS_NULL_IS_NULL_IS_EMPTY
                    ),
                    listFilteredValueExtractor("prior event",
                            e -> {
                                final History event = e.getEvent();
                                return event == null ? Collections.emptyList() : Collections.singletonList(event.getPriorRelated());
                            },
                            e -> true,
                            Enum::name
                    ),
                    setHasValueExtractor("descends from magna carta?",
                            e -> {
                                final History event = e.getEvent();
                                if (event == null) {
                                    return EnumSet.noneOf(History.class);
                                }
                                final History prior = event.getPriorRelated();
                                if (prior == null) {
                                    return EnumSet.noneOf(History.class);
                                } else {
                                    return EnumSet.of(prior);
                                }
                            },
                            MAGNA_CARTA
                    ),
                    new DumpExtractor<TestPojo>("years",
                            e -> {
                                final History event = e.getEvent();
                                return event == null ? "" : event.year();
                            },
                            Function.identity()
                    ).aggregator(summingAggregator()),
                    stringableExtractor(WEIGHTING)
                            .aggregator(numericAggregator(
                                    null,
                                    BigDecimal::add
                            )),
                    stringableExtractor(STEVE)
                            .aggregator(summingAggregator()),
                    stringableExtractor("antisteve", STEVE)
                    .aggregator(numericAggregator(
                            new BigDecimal("100.00"),
                            BigDecimal::subtract
                    ))
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
        underTest.dump((File)null, pojoRepository);
    }

    private final List<TestPojo> someBadPojos() {
        return Arrays.asList(
                new TestPojo().setId(0).setComment("fun").setEvent(DECLARATION_OF_INDEPENDENCE).setWeighting(new BigDecimal("0.2")).setSteve(5),
                new TestPojo().setId(1),
                new TestPojo()
        );
    }

    @Test
    public void canDumpSomePojosWithNullsInThem() {
        when(pojoRepository.getUnsafe("")).thenReturn(someBadPojos());
        underTest.dump((File)null, pojoRepository);
    }

    private final List<TestPojo> someGoodPojos() {
        return Arrays.asList(
                new TestPojo().setId(0).setEvent(MAGNA_CARTA)
                        .setWeighting(new BigDecimal("2")).setComment("Important.").setSteve(12),
                new TestPojo().setId(1).setEvent(COLUMBUS)
                        .setWeighting(new BigDecimal("1")).setComment("Happened.").setSteve(2),
                new TestPojo().setId(0).setEvent(DECLARATION_OF_INDEPENDENCE)
                        .setWeighting(new BigDecimal("0.3")).setComment("Tax protest, more or less.").setSteve(32)
        );
    }

    @Test
    public void canDumpSomePojosWithoutNullsInThem() {
        when(pojoRepository.getUnsafe("")).thenReturn(someGoodPojos());
        underTest.dump((File)null, pojoRepository);
    }

    @Test
    public void weCouldFilterADumpIfWeWereUsingARealDatabaseInThisTestToo() {
        when(pojoRepository.getUnsafe(" WHERE id > ? AND id < ?", 10, 20)).thenReturn(someGoodPojos());
        underTest.dump((File)null, pojoRepository, filter(ID, GT, 10), filter(ID, LT, 20));
    }

    @AllArgsConstructor
    private class Gathering {
        final PrintStream out;
        final ByteArrayOutputStream buf;
    }

    private Gathering gatherer() {
        val buf = new ByteArrayOutputStream();
        return new Gathering(new PrintStream(buf), buf);
    }

    @Test
    public void canDoSums() {
        when(pojoRepository.getUnsafe("")).thenReturn(someGoodPojos());
        val gatherer = gatherer();
        underTest.dump(gatherer.out, pojoRepository);
        gatherer.out.flush();
        final String out = new String(gatherer.buf.toByteArray(), StandardCharsets.UTF_8);
        final String[] lines = out.split(System.lineSeparator());
        System.out.println(lines[4]);
        assertThat(lines.length, is(5));
        assertThat(lines[4], containsString(",4484,"));
        assertThat(lines[4], containsString(",3.30,"));
        assertThat(lines[4], containsString(",46,"));
        assertThat(lines[4], endsWith(",54.00"));
    }
}
