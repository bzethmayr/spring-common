package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class TestOrder {
    private Integer id;
    private Integer userId;
    private TestUser user;
    private Instant orderedAt;
    private List<TestOrderItem> items = new ArrayList<>();
    private TestOrderSummary summary;
}
