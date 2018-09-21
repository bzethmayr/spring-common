package net.zethmayr.benjamin.spring.common.repository;

import net.zethmayr.benjamin.spring.common.repository.base.AbstractSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestSchemaService extends AbstractSchemaService {
    public TestSchemaService(final @Autowired JdbcTemplate db) {
        super(db);
    }
}
