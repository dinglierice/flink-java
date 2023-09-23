package com.alibaba.fliggy.orcas.statejob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/23 15:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThresholdUpdate {
    private String id;
    private Double threshold;
}
