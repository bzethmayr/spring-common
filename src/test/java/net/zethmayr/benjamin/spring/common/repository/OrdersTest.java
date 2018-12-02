package net.zethmayr.benjamin.spring.common.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestItem;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
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

import java.time.Instant;

import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.ABOUT_A_HUNDRED_DOLLARS;
import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.withItemIds;
import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.withItems;
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

    @Test
    public void canNotGetAThingThatIsNotThere() {
        val read = orders.get(42);
        assertThat(read.isPresent(), is(false));
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


    @Test
    public void prettySoonIBetterThinkAboutFullRecursion() throws Exception {
        val now = Instant.now();
        val order = withItems(now);
        val id = orders.insert(order);
//        verify(users).insert(order.getUser());
//        verify(items).insert(order.getItems().get(0).getItem());
//        verify(orderItems).insert(order.getItems().get(0));
//        verify(items).insert(order.getItems().get(1).getItem());
//        verify(orderItems).insert(order.getItems().get(1));
//        verify(items).insert(order.getItems().get(2).getItem());
//        verify(orderItems).insert(order.getItems().get(2));
//        verify(orderSummaries).insert(order.getSummary());
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

    @Test
    public void canInsertThenDelete() throws Exception {
        val now = Instant.now();
        val withItems = withItems(now);

        val orderId = orders.insert(withItems);
        val read = orders.get(orderId).orElseThrow(Exception::new);
        assertIsExpectedOrder(read, now, orderId);
        val userRead = users.get(read.getUserId()).orElseThrow(Exception::new);
        assertThat(userRead, isA(TestUser.class));
        val orderItemsRead = orderItems.getAll();
        assertThat(orderItemsRead, hasSize(3));
        val itemsRead = items.getAll();
        assertThat(itemsRead, hasSize(3));
        val summariesRead = orderSummaries.getAll();
        assertThat(summariesRead, hasSize(1));

        orders.delete(orderId);
        val readAgain = orders.get(orderId);
        assertThat(readAgain.isPresent(), is(false));
        val userReadAgain = users.get(read.getUserId());
        assertThat(userReadAgain.isPresent(), is(false)); // well, we did tell it to, vs to not to
        val orderItemsReadAgain = orderItems.getAll();
        assertThat(orderItemsReadAgain, hasSize(0));
        // BLARGH! USE_PARENT_ID is not recursive... since the joined repository does not use that id...
        // I could rebind for that index hhos.
        val itemsReadAgain = items.getAll();
        assertThat(itemsReadAgain, hasSize(0)); // again, we did tell it to. We could have said not to.
        // Now, I strongly suspect that MATERIALIZE_PARENT is recursive, but rewriting the mapper would be cheating.
        // both cases _"should"_ recurse.
        val summariesReadAgain = orderSummaries.getAll();
        assertThat(summariesReadAgain, hasSize(0));
    }
}

