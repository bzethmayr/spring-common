package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestOrderMapper;
import net.zethmayr.benjamin.spring.common.model.TestOrder;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestOrderRepository extends JoiningRepository<TestOrder, Integer> {
    public TestOrderRepository(final @Autowired JdbcTemplate jdbcTemplate, final @Autowired TestOrderItemRepository orderItems, final @Autowired TestUserRepository users) {
        super(jdbcTemplate, new TestOrderMapper(), new CoreRepository(jdbcTemplate), orderItems.primary, users);
    }

    public static class CoreRepository extends MapperRepository<TestOrder, Integer> {
        public CoreRepository(final JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate, new TestOrderMapper.CoreMapper(), TestOrderMapper.CoreMapper.ID);
        }
    }
}
