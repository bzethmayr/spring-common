package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PUBLIC;

@Getter(PUBLIC)
@Setter(PUBLIC)
@Accessors(chain = true)
public class SelfLinkyPojo {
    private Integer id;
    private String name;
    private Integer group;
    private List<SelfLinkyPojo> neighbors = new ArrayList<>();
    private Integer owns;
    private SelfLinkyPojo owned;
    private List<SelfLinkyPojo> owners = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelfLinkyPojo that = (SelfLinkyPojo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(group, that.group) &&
                Objects.equals(neighbors, that.neighbors) &&
                Objects.equals(owns, that.owns) &&
                Objects.equals(owned, that.owned) &&
                Objects.equals(owners, that.owners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, group, neighbors, owns, owned, owners);
    }
}
