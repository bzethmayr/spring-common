package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestItemMapper;
import net.zethmayr.benjamin.spring.common.model.TestItem;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestItemRepository extends MapperRepository<TestItem, Integer> {
    public TestItemRepository(final @Autowired JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new TestItemMapper(), TestItemMapper.ID);
    }
}
