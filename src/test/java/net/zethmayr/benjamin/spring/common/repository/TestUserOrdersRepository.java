package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestUserOrdersMapper;
import net.zethmayr.benjamin.spring.common.model.TestUser;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestUserOrdersRepository extends JoiningRepository<TestUser, Integer> {
    public TestUserOrdersRepository(
            final @Autowired JdbcTemplate jdbcTemplate,
            final @Autowired TestUserRepository baseUsers,
            final @Autowired TestOrderRepository orders) {
        super(jdbcTemplate, new TestUserOrdersMapper(), baseUsers, orders);
    }
}
