package com.alibaba.fliggy.orcas.graal.config;

import com.taobao.diamond.client.Diamond;
import com.taobao.diamond.manager.ManagerListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.io.IOException;

/**
 * @description: 通过Diamond配置的js脚本
 * @date: 2023/8/9 10:19
 * @author: dinglie
 */
public class DiamondFunctionConfigurationHolder extends AbstractDiamondConfig{
    @Override
    public void init() {
        try {
            String configInfo = Diamond.getConfig("test-script-config", "ds", 3000);
            updateScript(configInfo);
        } catch (IOException | ScriptException e1) {
            e1.printStackTrace();
        }

        // 监听配置：配置变化会立即推送最新值（异步非阻塞）
        Diamond.addListener("test-script-config", "ds", new ManagerListenerAdapter() {
            public void receiveConfigInfo(String configInfo) {
                //推空保护
                if (StringUtils.isBlank(configInfo)) {
                    return;
                }
                try {
                    updateScript(configInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateScript(String configInfo) throws ScriptException {
        scriptEngine.eval(configInfo);
        inv = (Invocable) scriptEngine;
    }
}
