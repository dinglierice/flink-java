# java并发编程

## 1、简介
不安全的线程
```java
@NotThreadSafe
public class UnsafeSequence {
    private int value;

    public int getValue() {
        return value ++ ;
    }
}
```
安全的线程
```java
public class SafeSequence {
    @GuardedBy("this") int value;

    public synchronized int getValue() {
        return value ++ ;
    }
}
```



