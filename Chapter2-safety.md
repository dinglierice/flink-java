# 第二章 线程安全

## 2.1 什么是线程安全
多个线程访问某个类时，如果这个类始终能做出符合预期的表现，这个类就是线程安全的

无状态的方法一定线程安全
```java
public class StatelessFactorizer implements Servlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        BigInteger i = extractFromReq(servletRequest);
        BigInteger[] factors = factor(i);
        encodeIntoResp(servletResponse, factors);
    }
}
```

## 2.2 原子性
```java
@NotThreadSafe
public class UnsafeCountFactorizer implements Servlet {
    private long count = 0;

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        count++;
    }
}
```
竞态条件的典型案例  
某一个线程读取count时，count值可能已经被另一个线程修改

### 2.2.1 竞态条件
典型：先检查后执行，执行结果依赖一个可能已经失效的观测结果  
### 2.2.2 延迟初始化中的竞态条件
```java
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
```
### 2.2.3 复合操作
a.包含一组必须以原子方式执行的操作以确保线程的安全性  
b.在一个无状态的添加一个状态时，如果该状态完全由线程安全的对象来管理，那么这个类仍然是线程安全的

## 2.3 加锁机制
书接上文 状态数量多于一个时不能保障上述2.2.3-b的成立

```java
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.Servlet;

@NotThreadSafe
public class UnsafeCachingFactorizer implements Servlet {
    private final AtomicReference<BigInteger> lastNumber = new AtomicReference<>();

    private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<>();
    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        BigInteger bigInteger = extractFromReq(servletRequest);
        if (bigInteger.equals(lastNumber.get())) {
            encodeIntoResp(servletResponse, lastFactors.get());
        } else {
            BigInteger i = extractFromReq(servletRequest);
            BigInteger[] factors = factor(i);
            encodeIntoResp(servletResponse, factors);
        }
    }
}
```
线程安全的定义：不变性不受破坏 => 乘机不等于因子之乘，破坏了两者间的逻辑约束  
更新lastNumber和更新lastFactors可能会导致不变性被破坏  
> 要保证状态的一致性，就需要在单个原子操作中更新所有的状态变量

### 2.3.1 内置锁
锁的组成：
+ 对象引用
+ 锁保护的代码块

> synchronized修饰的方法->锁住整个方法代码块，锁即调用该方法的对象
> synchronized修饰的静态方法->锁即classObject
```java
public class LockDemo {
    public static void main(String[] args) {
        synchronized (this) {
            System.out.println("doSomeThing");
        }
    }
}
```
每个java对象都可以被用作一个实现同步的锁，这些锁被称为内置锁或者监视器锁  
  
基于锁机制实现一个线程安全的代码

```java
import javax.annotation.concurrent.GuardedBy;
import java.math.BigInteger;

@ThreadSafe
public class SafeCachingFactorizer implements Servlet {
    @GuardedBy("this") private final BigInteger lastNumber;
    @GuardedBy("this") private final BigInteger[] lastFactors;

    @Override
    public synchronized void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        BigInteger bigInteger = extractFromReq(servletRequest);
        if (bigInteger.equals(lastNumber)) {
            encodeIntoResp(servletResponse, lastFactors);
        } else {
            BigInteger i = extractFromReq(servletRequest);
            BigInteger[] factors = factor(i);
            encodeIntoResp(servletResponse, factors);
        }
    }
}
```
### 2.3.2 重入
定义：线程可以获取由它自己持有的锁  
获取锁的操作的粒度是线程  
计数重入 获得锁：0-1-2 释放锁：2-1-0

```java
public class Widget {
    public synchronized void doSomething() {
        System.out.println("do something");
    }
}

public class LoggingWidget extends Widget {
    public synchronized void doSomething() {
        super.doSomething();
    }
}
```

如果没有重入机制，上述代码会产生死锁
## 2.4 用锁来保护状态




