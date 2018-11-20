package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TestOrderSummary {
    private Integer id;
    private Integer orderId;
    private String summary;
}
