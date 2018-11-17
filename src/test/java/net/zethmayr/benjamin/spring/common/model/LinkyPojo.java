package net.zethmayr.benjamin.spring.common.model;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@ToString
public class LinkyPojo {
    private int id;
    private int link;
    private String name;
    private TestPojo top;
    private List<TestPojo> left = new ArrayList<>();
}