package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.LinkyPojoMapper;
import net.zethmayr.benjamin.spring.common.model.LinkyPojo;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LinkyPojoRepository extends JoiningRepository<LinkyPojo, Integer> {
    static class CoreRepository extends MapperRepository<LinkyPojo, Integer> {
        CoreRepository(final JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate, new LinkyPojoMapper.CoreMapper(), LinkyPojoMapper.CoreMapper.ID);
        }
    }

    public LinkyPojoRepository(final @Autowired JdbcTemplate jdbcTemplate, final @Autowired TestPojoRepository supplemental) {
        super(jdbcTemplate, new LinkyPojoMapper(), new CoreRepository(jdbcTemplate), supplemental);
    }
}
