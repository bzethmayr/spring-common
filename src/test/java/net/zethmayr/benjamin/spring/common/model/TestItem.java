package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class TestItem {
    private int id;
    private String name;
    private BigDecimal price;
}
