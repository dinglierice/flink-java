package com.alibaba.fliggy.orcas.flink.connector;

import com.alibaba.fliggy.orcas.flink.statejob.TempSensorRecord;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * @description: 自定义数据源和数据汇
 * @date: 2023/9/30 15:17
 * @author: dinglie
 */
public class UDConnector {
    public static void main(String[] args) throws IOException {
        // 自定义数据汇
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        // 初始化两个事件
        DataStreamSource<TempSensorRecord> tempSensorRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("1", 21d, 123382945L),
                new TempSensorRecord("2", 23d, 123382945L),
                new TempSensorRecord("2", 24d, 123382946L),
                new TempSensorRecord("3", 29d, 1254642452L)
        );

        tempSensorRecordDataStreamSource.addSink(new SimpleSocketSink("localhost", 9091)).setParallelism(1);
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



class SimpleSocketSink extends RichSinkFunction<TempSensorRecord> {
    private final Socket socket;
    private final PrintStream writer;

    public SimpleSocketSink(String host, Integer port) throws IOException {
        socket = new Socket(InetAddress.getByName(host), port);
        writer = new PrintStream(socket.getOutputStream());
    }
    @Override
    public void invoke(TempSensorRecord value, Context context) throws Exception {
        writer.println(value.toString());
        writer.flush();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        writer.close();
        socket.close();
    }
}

class DerbyUpsertSink extends RichSinkFunction<TempSensorRecord> {
    private Connection conn;
    private PreparedStatement insertStmt;
    private PreparedStatement updateStmt;
    @Override
    public void open(Configuration parameters) throws Exception {
        conn = DriverManager.getConnection("jdbc:derby:memory: flinkExample", new Properties());
        insertStmt = conn.prepareStatement("INSERT INTO Temperatures (sensor, temp) VALUES (?, ?)");
        updateStmt = conn.prepareStatement("UPDATE Temperatures SET temp = ? where sensor = ?");
        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        insertStmt.close();
        updateStmt.close();
        conn.close();;
    }

    @Override
    public void invoke(TempSensorRecord value, Context context) throws Exception {
        updateStmt.setDouble(1, value.getTemp());
        updateStmt.setString(2, value.getDeviceId());
        updateStmt.execute();

        if (0 == updateStmt.getUpdateCount()) {
            insertStmt.setDouble(1, value.getTemp());
            insertStmt.setString(2, value.getDeviceId());
            insertStmt.execute();
        }
    }
}