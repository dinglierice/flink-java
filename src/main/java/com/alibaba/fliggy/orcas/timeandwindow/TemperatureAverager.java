package com.alibaba.fliggy.orcas.timeandwindow;

import org.apache.flink.api.common.functions.AggregateFunction;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/16 17:30
 */
public class TemperatureAverager<IN, ACC, OUT> implements AggregateFunction<IN, ACC, OUT> {
    @Override
    public ACC createAccumulator() {
        return null;
    }

    @Override
    public ACC add(IN in, ACC acc) {
        return null;
    }

    @Override
    public OUT getResult(ACC acc) {
        return null;
    }

    @Override
    public ACC merge(ACC acc, ACC acc1) {
        return null;
    }
}
