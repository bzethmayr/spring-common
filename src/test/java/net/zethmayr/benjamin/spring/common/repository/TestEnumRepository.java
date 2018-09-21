package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestEnumMapper;
import net.zethmayr.benjamin.spring.common.model.TestEnum;
import net.zethmayr.benjamin.spring.common.repository.base.EnumMapperRepository;
import org.springframework.stereotype.Service;

@Service
public class TestEnumRepository extends EnumMapperRepository<TestEnum> {
    public TestEnumRepository() {
        super(new TestEnumMapper());
    }
}
