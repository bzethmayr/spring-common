package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestPojoRepository extends MapperRepository<TestPojo, Integer> {

    public TestPojoRepository(final @Autowired JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new TestPojoMapper(), TestPojoMapper.ID);
    }
}
