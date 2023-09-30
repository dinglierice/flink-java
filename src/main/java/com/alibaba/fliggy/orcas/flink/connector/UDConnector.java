package com.alibaba.fliggy.orcas.flink.connector;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @description: 自定义数据源和数据汇
 * @date: 2023/9/30 15:17
 * @author: dinglie
 */
public class UDConnector {
    public static void main(String[] args) {
        // 自定义数据汇

    }
}


class CountSource implements SourceFunction<Long> {
    private boolean isRunning = true;

    @Override
    public void run(SourceContext<Long> sourceContext) throws Exception {
        long cnt = -1L;
        while(cnt < Long.MAX_VALUE) {
            cnt ++ ;
            sourceContext.collect(cnt);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}


class ResettableCountSource implements SourceFunction<Long>, CheckpointedFunction {
    private boolean isRunning = true;
    private long cnt;
    private ListState<Long> offsetState;
    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        offsetState.clear();
        offsetState.add(cnt);
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {
        ListStateDescriptor<Long> offset = new ListStateDescriptor<>("offset", Long.class);
        ListState<Long> listState = functionInitializationContext.getOperatorStateStore().getListState(offset);
        Iterable<Long> longs = listState.get();
        cnt = longs == null || !longs.iterator().hasNext() ? -1L:longs.iterator().next();
    }

    @Override
    public void run(SourceContext<Long> sourceContext) throws Exception {
        while(isRunning && cnt < Long.MAX_VALUE) {
            synchronized (sourceContext.getCheckpointLock()) {
                cnt ++ ;
                sourceContext.collect(cnt);
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}


