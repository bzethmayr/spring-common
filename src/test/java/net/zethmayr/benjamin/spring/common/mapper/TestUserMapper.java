package net.zethmayr.benjamin.spring.common.mapper;

import net.zethmayr.benjamin.spring.common.mapper.base.ClassFieldMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.ColumnType;
import net.zethmayr.benjamin.spring.common.mapper.base.ComposedMapper;
import net.zethmayr.benjamin.spring.common.mapper.base.InvertibleRowMapperBase;
import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.model.TestUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestUserMapper extends InvertibleRowMapperBase<TestUser> {
    public static Mapper<TestUser, Integer, Integer> ID = ComposedMapper.simpleField(
            "id",
            TestUser::getId,
            ColumnType.INTEGER_INDEX,
            TestUser::setId
    );
    public static Mapper<TestUser, String, String> NAME = ComposedMapper.simpleField(
            "user_name",
            TestUser::getName,
            ColumnType.LONG_STRING,
            TestUser::setName
    );
    public static final List<ClassFieldMapper<TestUser>> FIELDS = Collections.unmodifiableList(Arrays.asList(
            ID, NAME
    ));
    public static final String TABLE = "users";

    public TestUserMapper() {
        super(TestUser.class, FIELDS, TABLE);
    }

    @Override
    public TestUser empty() {
        return new TestUser();
    }
}
