package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.HistoryMapper;
import net.zethmayr.benjamin.spring.common.model.History;
import net.zethmayr.benjamin.spring.common.repository.base.EnumMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HistoryRepository extends EnumMapperRepository<History> {
    public HistoryRepository(final @Autowired JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new HistoryMapper());
    }
}
