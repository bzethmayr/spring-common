package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.stereotype.Service;

@Service
public class TestPojoRepository extends MapperRepository<TestPojo, Integer> {
    public TestPojoRepository() {
        super(new TestPojoMapper(), TestPojoMapper.ID);
    }
}
