package net.zethmayr.benjamin.spring.common.repository.base;

public interface EnumRepository<T extends Enum<T>> extends Repository<T, Integer> {
}
