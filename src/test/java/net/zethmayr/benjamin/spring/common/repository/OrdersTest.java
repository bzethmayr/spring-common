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
import org.springframework.boot.test.mock.mockito.SpyBean;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OrdersTest {
    @Autowired
    private TestSchemaService schemaService;

    @SpyBean
    private TestUserRepository users;

    @Autowired
    private TestOrderRepository orders;

    @SpyBean
    private TestOrderItemRepository orderItems;

    @SpyBean
    private TestOrderSummaryRepository orderSummaries;

    @SpyBean
    private TestItemRepository items;

    private static final BigDecimal FIVE_DOLLARS = new BigDecimal("5.00");
    private static final BigDecimal ABOUT_A_HUNDRED_DOLLARS = new BigDecimal("99.95");
    private static final BigDecimal THREE_FIFTY = new BigDecimal("3.50");

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
        val summaries = orderSummaries.getAll();
        assertThat(userList, is(empty()));
        assertThat(orderList, is(empty()));
        assertThat(orderItemsList, is(empty()));
        assertThat(itemsList, is(empty()));
        assertThat(summaries, is(empty()));
    }

    public Matcher<String> containsJoinFor(final String table) {
        return containsString("JOIN " + table);
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

    private TestOrder asOf(final Instant now) {
        return new TestOrder()
                .setOrderedAt(now)
                .setSummary(new TestOrderSummary().setSummary("Hasty and suspicious"));

    }

    private TestOrder withItems(final Instant now) {
        return asOf(now)
                .setUser(new TestUser().setName("Yarn Bean"))
                .setItems(Arrays.asList(
                        new TestOrderItem().setItem(new TestItem().setName("Soap").setPrice(FIVE_DOLLARS)).setQuantity(2),
                        new TestOrderItem().setItem(new TestItem().setName("Cheese").setPrice(ABOUT_A_HUNDRED_DOLLARS)).setQuantity(1),
                        new TestOrderItem().setItem(new TestItem().setName("Soda").setPrice(THREE_FIFTY)).setQuantity(12)
                ));
    }

    private TestOrder withItemIds(final Instant now) {
        return asOf(now)
                .setUserId(1)
                .setItems(Arrays.asList(
                        new TestOrderItem().setItemId(1).setQuantity(2),
                        new TestOrderItem().setItemId(2).setQuantity(1),
                        new TestOrderItem().setItemId(3).setQuantity(12)
                ));
    }

    @Test
    public void prettySoonIBetterThinkAboutFullRecursion() throws Exception {
        val now = Instant.now();
        val order = withItems(now);
        val id = orders.insert(order);
        verify(users).insert(order.getUser());
        verify(items).insert(order.getItems().get(0).getItem());
        verify(orderItems).insert(order.getItems().get(0));
        verify(items).insert(order.getItems().get(1).getItem());
        verify(orderItems).insert(order.getItems().get(1));
        verify(items).insert(order.getItems().get(2).getItem());
        verify(orderItems).insert(order.getItems().get(2));
        verify(orderSummaries).insert(order.getSummary());
        LOG.info("id is {}", id);
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
        reset(users, orderItems, orderSummaries, items);

        val read = orders.get(id).orElseThrow(Exception::new);
        verifyNoMoreInteractions(users, orderItems, orderSummaries, items);
        assertIsExpectedOrder(read, now, id);
    }

    private void assertIsExpectedOrder(final TestOrder read, final Instant now, final Integer id) {
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

    @Test
    public void canAvoidInsertion() {
        val now = Instant.now();
        val withItems = withItems(now);
        val withIds = withItemIds(now);
        val anotherWithIds = withItemIds(now);
        val startInsert = System.nanoTime();
        val idInitial = orders.insert(withItems);
        val idSecond = orders.insert(withIds);
        val idThird = orders.insert(anotherWithIds);
        LOG.info("inserted in about {}ns", System.nanoTime() - startInsert);
        val startRead = System.nanoTime();
        val all = orders.getAll();
        LOG.info("got all in about {}ns", System.nanoTime() - startRead);
        assertIsExpectedOrder(all.get(0), now, idInitial);
        assertIsExpectedOrder(all.get(1), now, idSecond);
        assertIsExpectedOrder(all.get(2), now, idThird);
    }
}

