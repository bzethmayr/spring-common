package net.zethmayr.benjamin.spring.common.repository;

import lombok.val;
import net.zethmayr.benjamin.spring.common.model.TestUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Arrays;

import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.changeSummary;
import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.userNamed;
import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.withItemIds;
import static net.zethmayr.benjamin.spring.common.repository.FakeOrders.withItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserOrdersTest {
    @Autowired
    private TestUserOrdersRepository underTest;

    @SpyBean
    private TestOrderRepository orders;

    @SpyBean
    private TestUserRepository users;

    @SpyBean
    private TestOrderItemRepository orderItems; // transitive...

    @SpyBean
    private TestItemRepository items; // transitive

    @SpyBean
    private TestOrderSummaryRepository summaries; // transitive

    @Autowired
    private TestSchemaService schemaService;

    @Before
    public void setUp() {
        //TODO: we'd like to be able to have transitives "just happen..."
        schemaService.nuke(orders, users, orderItems, items, summaries);
        schemaService.applySchemaFor(orders, users, orderItems, items, summaries);
    }

    @Test
    public void wires() {
        assertThat(underTest, isA(TestUserOrdersRepository.class));
    }

    @Test
    public void canInsertAUserWithOrders() throws Exception {
        val now = Instant.now();
        final TestUser user = userNamed("Also Yarn Bean");
        user.setOrders(Arrays.asList(
                changeSummary(withItems(now).setUser(null), "What is this cheese?"),
                changeSummary(withItemIds(now).setUserId(null), "Again the cheese."),
                withItemIds(now).setUserId(null)
        ));
        val id = underTest.insert(user);
        verify(users).insert(user);
        verify(orders, times(3)).insert(any());
        verify(summaries, times(3)).insert(any());
        verify(items, times(3)).insert(any());
        verify(orderItems, times(9)).insert(any());
        // that went well. but insert recursion was already known to work.
        final TestUser read = underTest.get(id).orElseThrow(Exception::new);
        assertThat(read.getOrders(), hasSize(3));
        for (val order : read.getOrders()) {
            val items = order.getItems();
            assertThat(items, hasSize(3));
            assertThat(items.get(0).getItem().getName(), is("Soap"));
            assertThat(items.get(1).getItem().getName(), is("Cheese"));
            assertThat(items.get(2).getItem().getName(), is("Soda"));
            assertThat(items.get(2).getQuantity(), is(12));
        }
        // golly. issues present in stitching and in deduplication
        val allUsers = users.getAll();
        assertThat(allUsers, hasSize(1));
        assertThat(allUsers.get(0).getName(), is(user.getName()));
    }
}
