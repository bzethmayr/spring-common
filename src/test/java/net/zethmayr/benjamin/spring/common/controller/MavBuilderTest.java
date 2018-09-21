package net.zethmayr.benjamin.spring.common.controller;

import lombok.val;
import net.zethmayr.benjamin.spring.common.controller.base.MavBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;

public class MavBuilderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String EXPECTED_VIEW = "foo";

    @Test
    public void canBuildAnInstance() {
        val underTest = MavBuilder.blank();
        assertThat(underTest, isA(MavBuilder.class));
        assertThat(underTest.viewName(EXPECTED_VIEW).status(I_AM_A_TEAPOT), isA(MavBuilder.class));
        val built = underTest.build();
        assertThat(built, isA(ModelAndView.class));
        assertThat(built.getViewName(), is(EXPECTED_VIEW));
        assertThat(built.getStatus(), is(I_AM_A_TEAPOT));
    }

    @Test
    public void laterActionsHappenAtBuild() {
        val underTest = MavBuilder.blank();
        val flag = new AtomicBoolean(false);
        underTest.later(builder -> {
            assertThat(builder, isA(MavBuilder.class));
            flag.set(true);
        });
        assertThat(flag.get(), is(false));
        underTest.build();
        assertThat(flag.get(), is(true));
    }

    @Test
    public void ifLaterActionThrowsBuildThrows() {
        val marker = new RuntimeException();
        thrown.expect(sameInstance(marker));
        val underTest = MavBuilder.blank();
        assertThat(underTest.later(b -> {
            throw marker;
        }), is(sameInstance(underTest)));
        underTest.build();
    }

    @Test
    public void canBuildAnInstanceToView() {
        val underTest = MavBuilder.to(EXPECTED_VIEW);
        val built = underTest.build();
        assertThat(built.getViewName(), is(EXPECTED_VIEW));
    }

    @Test
    public void canPutValuesInModel() {
        val underTest = MavBuilder.blank();
        underTest.put("flag", true);
        val built = underTest.build();
        assertThat(built.getModel().get("flag"), is(true));
    }
}
