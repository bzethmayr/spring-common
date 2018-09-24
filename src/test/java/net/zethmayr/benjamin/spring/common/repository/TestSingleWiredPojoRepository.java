package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.TestPojoMapper;
import net.zethmayr.benjamin.spring.common.model.TestPojo;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.stereotype.Service;

@Service
public class TestSingleWiredPojoRepository extends MapperRepository.SingleWired<TestPojo, Integer> {
    public TestSingleWiredPojoRepository() {
        super(new TestPojoMapper(), TestPojoMapper.ID);
    }
}
