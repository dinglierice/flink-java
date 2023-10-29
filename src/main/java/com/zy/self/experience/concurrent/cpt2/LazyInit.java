package com.zy.self.experience.concurrent.cpt2;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @description: 延迟初始化
 * @date: 2023/10/25 22:14
 * @author: dinglie
 */
@NotThreadSafe
public class LazyInit {
    private ExpansiveObject expansiveObject;

    public ExpansiveObject getInstance() {
        if (expansiveObject == null) {
            expansiveObject = new ExpansiveObject();
        }
        return expansiveObject;
    }
}


class ExpansiveObject {

}