package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.model.TestItem;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.model.TestOrderItem;
import net.zethmayr.benjamin.spring.common.model.TestOrderSummary;
import net.zethmayr.benjamin.spring.common.model.TestUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

public class FakeOrders {
    static final BigDecimal FIVE_DOLLARS = new BigDecimal("5.00");
    static final BigDecimal ABOUT_A_HUNDRED_DOLLARS = new BigDecimal("99.95");
    static final BigDecimal THREE_FIFTY = new BigDecimal("3.50");

    static TestUser userNamed(final String userName) {
        return new TestUser().setName(userName);
    }

    static TestOrder asOf(final Instant now) {
        return new TestOrder()
                .setOrderedAt(now)
                .setSummary(new TestOrderSummary().setSummary("Hasty and suspicious"));

    }

    static TestOrderItem withQuantity(final int quantity) {
        return new TestOrderItem().setQuantity(quantity);
    }

    static TestItem withNameAndPrice(final String name, final BigDecimal price) {
        return new TestItem().setName(name).setPrice(price);
    }

    static TestOrder withItems(final Instant now) {
        return asOf(now)
                .setUser(userNamed("Yarn Bean"))
                .setItems(Arrays.asList(
                        withQuantity(2).setItem(withNameAndPrice("Soap", FIVE_DOLLARS)),
                        withQuantity(1).setItem(withNameAndPrice("Cheese", ABOUT_A_HUNDRED_DOLLARS)),
                        withQuantity(12).setItem(withNameAndPrice("Soda", THREE_FIFTY))
                ));
    }

    static TestOrder changeSummary(final TestOrder order, final String summary) {
        order.getSummary().setSummary(summary);
        return order;
    }

    static TestOrder withItemIds(final Instant now) {
        return asOf(now)
                .setUserId(1)
                .setItems(Arrays.asList(
                        withQuantity(2).setItemId(1),
                        withQuantity(1).setItemId(2),
                        withQuantity(12).setItemId(3)
                ));
    }
}
