package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestUserMapper;
import net.zethmayr.benjamin.spring.common.model.TestUser;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestUserRepository extends MapperRepository<TestUser, Integer> {
    public TestUserRepository(final @Autowired JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new TestUserMapper(), TestUserMapper.ID);
    }
}
