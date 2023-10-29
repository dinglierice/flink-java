package com.zy.self.experience.flink.statejob;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/16 17:20
 */
@Data
@AllArgsConstructor
public class TempSensorRecord {
    private String deviceId;

    private Double temp;

    private Long eventTime;
}
