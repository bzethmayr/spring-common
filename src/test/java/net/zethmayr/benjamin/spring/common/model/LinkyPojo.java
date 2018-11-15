package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class LinkyPojo {
    private int id;
    private int link;
    private String name;
    private List<TestPojo> top = new ArrayList<>();
    private List<TestPojo> left = new ArrayList<>();
}