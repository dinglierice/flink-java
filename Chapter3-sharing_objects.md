# 对象的共享
## 3.1 可见性
```java

public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class RenderThread extends Thread {
        @Override
        public void run() {
            while (!ready) {
                Thread.yield();
            }
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        new RenderThread().start();
        number = 42;
        ready = true;
    }
}
```

其中Thread.yield();的含义：它让掉当前线程CPU 的时间片，使正在运行中的线程重新变成就绪状态，并重新竞争CPU 的调度权。 它可能会获取到，也有可能被其他线程获取到。  
  
这段代码的含义：启动一个线程，这个线程执行一个循环代码，当读取的变量ready变为false时，线程交出CPU的调度权，打印数字。如果main方法所在的线程永远无法抢占到线程，那么number永远都是为0。  
### 3.1.1 失效数据
```java
public class SyncMutableInteger {
    private int value;
    public synchronized int getValue() {
        return this.value;
    }
    public synchronized void setValue(int value) {
        this.value = value;
    }
}
```
!get也要同步代码块，否则仍然可能获取不到最新的value状态。
为什么？get过程中另一个线程获取了所修改变量状态

### 3.1.4 Volatile变量
将变量的更新操作通知到其他线程  
读取volatile变量时总是会返回最新的值
