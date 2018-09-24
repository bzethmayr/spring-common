package net.zethmayr.benjamin.spring.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@ToString
@EqualsAndHashCode
@Accessors(chain = true)
@Setter
@Getter
public class TestPojo {

    private Integer id;
    private History event;
    private String comment;
    private Integer steve;
    private BigDecimal weighting;

}
