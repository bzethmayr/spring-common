package net.zethmayr.benjamin.spring.common.model;

import java.math.BigDecimal;
import java.util.Objects;

public class TestPojo {

    private Integer id;
    private History event;
    private String comment;
    private BigDecimal weighting;

    public Integer getId() {
        return id;
    }

    public TestPojo setId(Integer id) {
        this.id = id;
        return this;
    }

    public History getEvent() {
        return event;
    }

    public TestPojo setEvent(History event) {
        this.event = event;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public TestPojo setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public BigDecimal getWeighting() {
        return weighting;
    }

    public TestPojo setWeighting(BigDecimal weighting) {
        this.weighting = weighting;
        return this;
    }

    @Override
    public String toString() {
        return "TestPojo{" +
                "id=" + id +
                ", event=" + event +
                ", comment='" + comment + '\'' +
                ", weighting=" + weighting +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestPojo testPojo = (TestPojo) o;
        return Objects.equals(id, testPojo.id) &&
                event == testPojo.event &&
                Objects.equals(comment, testPojo.comment) &&
                Objects.equals(weighting, testPojo.weighting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, event, comment, weighting);
    }
}
