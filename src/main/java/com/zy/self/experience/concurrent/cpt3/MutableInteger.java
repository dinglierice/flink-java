package com.zy.self.experience.concurrent.cpt3;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class MutableInteger {
    @Getter
    @Setter
    private int value;
}
