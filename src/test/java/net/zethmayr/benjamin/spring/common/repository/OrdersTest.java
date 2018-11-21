package net.zethmayr.benjamin.spring.common.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestItem;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.model.TestOrderItem;
import net.zethmayr.benjamin.spring.common.model.TestOrderSummary;
import net.zethmayr.benjamin.spring.common.model.TestUser;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.startsWith;
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
    private TestOrderSummaryRepository orderSummaries;

    @Autowired
    private TestItemRepository items;

    @Before
    public void setUp() {
        schemaService.nuke(users, orders.primary, orderItems.primary, items, orderSummaries);
        schemaService.applySchemaFor(users, orders.primary, orderItems.primary, items, orderSummaries);
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

    public Matcher<String> containsJoinFor(final String table) {
        return containsString("JOIN "+table);
    }

    @Test
    public void queryIncludesExpectedJoins() {
        val select = orders.select();
        LOG.info("select is {}", select);
        assertThat(select, containsString("FROM orders"));
        assertThat(select, containsJoinFor("users"));
        assertThat(select, containsJoinFor("order_items"));
        assertThat(select, containsJoinFor("items"));
        assertThat(select, containsJoinFor("order_summaries"));
    }

    @Test
    public void prettySoonIBetterThinkAboutFullRecursion() throws Exception {
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
                ))
                .setSummary(new TestOrderSummary().setSummary("Hasty and suspicious"));

        val id = orders.insert(order);
        val usersList = users.getAll();
        val ordersList = orders.getAll();
        val orderItemsList = orderItems.getAll();
        val summariesList = orderSummaries.getAll();
        val itemsList = items.getAll();
        assertThat(usersList, hasSize(1));
        assertThat(ordersList, hasSize(1));
        assertThat(orderItemsList, hasSize(3));
        assertThat(summariesList, hasSize(1));
        assertThat(itemsList, hasSize(3));

        val read = orders.get(id).orElseThrow(Exception::new);
        assertThat(read.getId(), is(id));
        assertThat(read.getOrderedAt(), is(now));
        assertThat(read.getUserId(), is(1));
        assertThat(read.getUser(), isA(TestUser.class));
        assertThat(read.getUser().getName(), is("Yarn Bean"));
        assertThat(read.getSummary(), isA(TestOrderSummary.class));
        assertThat(read.getSummary().getSummary(), startsWith("Hasty"));
        val items = read.getItems();
        assertThat(items, hasSize(3));
        val soap = items.get(0);
        assertThat(soap.getQuantity(), is(2));
        assertThat(soap.getItem(), isA(TestItem.class));
        assertThat(soap.getItem().getName(), is("Soap"));
        val cheeses = items.get(1);
        assertThat(cheeses.getQuantity(), is(1));
        val cheese = cheeses.getItem();
        assertThat(cheese, isA(TestItem.class));
        assertThat(cheese.getName(), is("Cheese"));
        assertThat(cheese.getPrice(), is(ABOUT_A_HUNDRED_DOLLARS));
        val soda = items.get(2);
        assertThat(soda.getQuantity(), is(12));
        assertThat(soda.getItem(), isA(TestItem.class));
        assertThat(soda.getItem().getName(), is("Soda"));
    }
}

