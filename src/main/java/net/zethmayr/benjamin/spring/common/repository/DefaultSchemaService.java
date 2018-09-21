package net.zethmayr.benjamin.spring.common.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.zethmayr.benjamin.spring.common.repository.base.AbstractSchemaService;
import net.zethmayr.benjamin.spring.common.repository.base.EnumRepository;
import net.zethmayr.benjamin.spring.common.repository.base.MapperRepository;
import net.zethmayr.benjamin.spring.common.repository.base.Repository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * A default implementation of {@link AbstractSchemaService} that <ul>
 *     <li>binds all repositories</li>
 *     <li>establishes schema for {@link EnumRepository} and {@link MapperRepository} instances</li>
 *     <li>inserts data for {@link EnumRepository} instances</li>
 * </ul> and provides methods to collectively truncate and drop repositories
 */
@Service
@ConditionalOnProperty("net.zethmayr.benjamin.repository.defaultSchemaService")
@Slf4j
public class DefaultSchemaService extends AbstractSchemaService implements InitializingBean {

    private final MapperRepository[] pojoRepositories;
    private final EnumRepository[] enumRepositories;

    public DefaultSchemaService(final @Autowired JdbcTemplate db, final @Autowired(required = false) Repository... repositories) {
        super(db);
        val pojoRepositories = new ArrayList<MapperRepository>();
        val enumRepositories = new ArrayList<EnumRepository>();
        if (repositories != null) {
            for (val repository : repositories) {
                if (repository instanceof EnumRepository) {
                    enumRepositories.add((EnumRepository) repository);
                    continue;
                }
                // mind the specificity. EnumRepository needs tested for first.
                if (repository instanceof MapperRepository) {
                    pojoRepositories.add((MapperRepository) repository);
                    continue;
                }
                LOG.warn("Unknown repository implementation. Maybe you should use a custom service.");
            }
        }
        this.pojoRepositories = pojoRepositories.toArray(new MapperRepository[]{});
        this.enumRepositories = enumRepositories.toArray(new EnumRepository[]{});
    }

    @Override
    @SuppressWarnings("unchecked") // because the generic types in the writeDataFor call are known to come from the same type token
    public void afterPropertiesSet() throws Exception {
        applyEnums();
        applyPojos();
    }

    public void applyEnums() {
        applySchemaFor(enumRepositories);
        for (val repository : enumRepositories) {
            writeDataFor(repository, repository.mapper().enumValues());
        }
    }

    public void applyPojos() {
        applySchemaFor(pojoRepositories);
    }

    public void burnEnums() {
        burn(enumRepositories);
    }

    public void burnPojos() {
        burn(pojoRepositories);
    }

    public void nukeEnums() {
        nuke(enumRepositories);
    }

    public void nukePojos() {
        nuke(pojoRepositories);
    }

    public void burnAll() {
        burnEnums();
        burnPojos();
    }

    public void nukeAll() {
        nukeEnums();
        nukePojos();
    }
}
