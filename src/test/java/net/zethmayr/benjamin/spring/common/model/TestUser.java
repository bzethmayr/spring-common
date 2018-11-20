package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class TestUser {
    private Integer id;
    private String name;
    private List<TestOrder> orders = new ArrayList<>();
}
