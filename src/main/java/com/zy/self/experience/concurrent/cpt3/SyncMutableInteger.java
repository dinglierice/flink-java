package com.zy.self.experience.concurrent.cpt3;

public class SyncMutableInteger {
    private int value;
    public synchronized int getValue() {
        return this.value;
    }
    public synchronized void setValue(int value) {
        this.value = value;
    }
}
