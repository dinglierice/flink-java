package com.alibaba.fliggy.orcas.concurrent.cpt1;

import javax.annotation.concurrent.GuardedBy;

/**
 * @description:
 * @date: 2023/10/24 23:55
 * @author: dinglie
 */
public class SafeSequence {
    @GuardedBy("this") int value;

    public synchronized int getValue() {
        return value ++ ;
    }
}
