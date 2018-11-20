package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TestOrderItem {
    private Integer id;
    private Integer orderId;
    private TestOrder order;
    private Integer itemId;
    private TestItem item;
    private Integer quantity = 1;
}
