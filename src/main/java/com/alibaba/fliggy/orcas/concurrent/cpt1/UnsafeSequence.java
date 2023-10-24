package com.alibaba.fliggy.orcas.concurrent.cpt1;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @description:
 * @date: 2023/10/24 23:54
 * @author: dinglie
 */
@NotThreadSafe
public class UnsafeSequence {
    private int value;

    public int getValue() {
        return value ++ ;
    }
}
