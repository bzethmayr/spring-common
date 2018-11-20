package net.zethmayr.benjamin.spring.common.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestItem;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.model.TestOrderItem;
import net.zethmayr.benjamin.spring.common.model.TestUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OrdersTest {
    @Autowired
    private TestSchemaService schemaService;

    @Autowired
    private TestUserRepository users;

    @Autowired
    private TestOrderRepository orders;

    @Autowired
    private TestOrderItemRepository orderItems;

    @Autowired
    private TestItemRepository items;

    @Before
    public void setUp() {
        schemaService.nuke(users, orders.primary, orderItems.primary, items);
        schemaService.applySchemaFor(users, orders.primary, orderItems.primary, items);
    }

    @Test
    public void allAreEmptyInitially() {
        val userList = users.getAll();
        val orderList = orders.getAll();
        val orderItemsList = orderItems.getAll();
        val itemsList = items.getAll();
        assertThat(userList, is(empty()));
        assertThat(orderList, is(empty()));
        assertThat(orderItemsList, is(empty()));
        assertThat(itemsList, is(empty()));
    }

    @Test
    public void prettySoonIBetterThinkAboutFullRecursion() {
        val now = Instant.now();
        val FIVE_DOLLARS = new BigDecimal("5.00");
        val ABOUT_A_HUNDRED_DOLLARS = new BigDecimal("99.95");
        val THREE_FIFTY = new BigDecimal("3.50");
        val order = new TestOrder()
                .setUser(new TestUser().setName("Yarn Bean"))
                .setOrderedAt(now)
                .setItems(Arrays.asList(
                        new TestOrderItem().setItem(new TestItem().setName("Soap").setPrice(FIVE_DOLLARS)).setQuantity(2),
                        new TestOrderItem().setItem(new TestItem().setName("Cheese").setPrice(ABOUT_A_HUNDRED_DOLLARS)).setQuantity(1),
                        new TestOrderItem().setItem(new TestItem().setName("Soda").setPrice(THREE_FIFTY)).setQuantity(12)
                ));
        val id = orders.insert(order);
        val usersList = users.getAll();
        val ordersList = orders.getAll();
        val orderItemsList = orderItems.getAll();
        assertThat(usersList, hasSize(1));
        assertThat(ordersList, hasSize(1));
        assertThat(orderItemsList, hasSize(3));
    }
}

