package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestOrderSummaryMapper;
import net.zethmayr.benjamin.spring.common.model.TestOrderSummary;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestOrderSummaryRepository extends MapperRepository<TestOrderSummary, Integer> {
    public TestOrderSummaryRepository(final @Autowired JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new TestOrderSummaryMapper(), TestOrderSummaryMapper.ID);
    }
}
