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

## 2.1 原子性
