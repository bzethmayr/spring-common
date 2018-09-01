package net.zethmayr.benjamin.spring.common.cli;

import java.util.Arrays;

public class Commanded {
    public void jump(String... args) {
        System.out.println("Jumping for " + Arrays.toString(args));
    }

    public void fall(String... args) {
        System.out.println("Falling from " + Arrays.toString(args));
    }
}
