package net.zethmayr.benjamin.spring.common.repository;

import lombok.extern.slf4j.Slf4j;
import net.zethmayr.benjamin.spring.common.mapper.TestEnumMapper;
import net.zethmayr.benjamin.spring.common.model.TestEnum;
import net.zethmayr.benjamin.spring.common.repository.base.EnumMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestSingleWiredEnumRepository extends EnumMapperRepository.SingleWired<TestEnum> {
    public TestSingleWiredEnumRepository() {
        super(new TestEnumMapper());
        LOG.debug("Called SingleWired implementation constructor...");
    }
}
