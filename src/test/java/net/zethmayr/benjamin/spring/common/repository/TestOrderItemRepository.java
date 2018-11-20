package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestOrderItemMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.JoiningRowMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.TestOrderItem;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestOrderItemRepository extends JoiningRepository<TestOrderItem, Integer> {
    private static class CoreRepository extends MapperRepository<TestOrderItem, Integer> {

        public CoreRepository(final JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate, new TestOrderItemMapper.CoreMapper(), TestOrderItemMapper.CoreMapper.ID);
        }
    }

    public TestOrderItemRepository(final @Autowired JdbcTemplate jdbcTemplate, final @Autowired TestItemRepository itemsRepo) {
        super(jdbcTemplate, new TestOrderItemMapper(), new CoreRepository(jdbcTemplate), itemsRepo);
    }
}
