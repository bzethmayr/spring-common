package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.mapper.SelfLinkyPojoMapper;
import net.zethmayr.benjamin.spring.common.model.SelfLinkyPojo;
import net.zethmayr.benjamin.spring.common.repository.base.JoiningRepository;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import static net.zethmayr.benjamin.spring.common.mapper.SelfLinkyPojoMapper.CoreMapper.ID;

@Service
public class SelfLinkyPojoRepository extends JoiningRepository<SelfLinkyPojo, Integer> {
    private static class CoreRepository extends MapperRepository<SelfLinkyPojo, Integer> {
        private CoreRepository(final JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate, new SelfLinkyPojoMapper.CoreMapper(), ID);
        }
    }

    public SelfLinkyPojoRepository(final @Autowired JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new SelfLinkyPojoMapper(), new CoreRepository(jdbcTemplate));
    }
}
