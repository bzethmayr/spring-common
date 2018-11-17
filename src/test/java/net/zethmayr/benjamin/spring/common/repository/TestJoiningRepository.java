package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestJoiningMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestJoiningRepository extends JoiningRepository<TestPojo, Integer> {
    protected TestJoiningRepository(final @Autowired JdbcTemplate jdbcTemplate,
        final @Autowired TestPojoRepository pojos,
        final @Autowired HistoryRepository enums) {
        super(jdbcTemplate, new TestJoiningMapper(), pojos, enums);
    }
}
