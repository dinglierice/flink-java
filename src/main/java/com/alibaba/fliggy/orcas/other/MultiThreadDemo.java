package com.alibaba.fliggy.orcas.other;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @description: 多线程实践
 * @date: 2023/10/3 17:24
 * @author: dinglie
 */
public class MultiThreadDemo {
    public static void main(String[] args) {
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 3 start!!!!!!");
                synchronized (this) {
                    try {
                        System.in.read();
                    } catch (Exception e) {
                    }
                    System.out.println("Thread 3 end!!!!!!");
                }
            }
        });
        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 4 start!!!!!!");
                synchronized (this) {
                    System.out.println("Thread 4 end!!!!!!");
                }
            }
        });
        Thread t5 = new Thread(new Runnable() {
            @Override
            public void run() {
                int a = 0;
                System.out.println("Thread 5 start!!!!!!");
                synchronized (this) {
                    a++;
                    System.out.println("Thread 5 end!!!!!!");
                }
            }
        });
        Thread t6 = new Thread(new Runnable() {
            @Override
            public void run() {
                int a = 0;
                System.out.println("Thread 6 start!!!!!!");
                synchronized (this) {
                    a++;
                    System.out.println("Thread 6 end!!!!!!");
                }
            }
        });
        t3.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
        }
        t4.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
        }
        t5.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
        }
        t6.start();
    }
}
