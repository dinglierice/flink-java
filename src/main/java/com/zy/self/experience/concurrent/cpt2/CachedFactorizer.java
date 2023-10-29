package com.zy.self.experience.concurrent.cpt2;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @description:
 * @date: 2023/10/25 00:35
 * @author: dinglie
 */
@ThreadSafe
public class CachedFactorizer implements Servlet {

    @GuardedBy("this") private BigInteger lastNumber;
    @GuardedBy("this") private BigInteger[] lastFactors;
    @GuardedBy("this")
    private long hits;
    @GuardedBy("this") private long cachedHits;

    public synchronized long getHits() {
        return hits;
    }

    public synchronized double getCachedHitsRatio() {
        return (double) cachedHits / hits;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        BigInteger bigInteger = extractFromReq(servletRequest);
        BigInteger[] factors = null;
        synchronized (this) {
            ++ hits;
            if (bigInteger.equals(lastNumber)) {
                ++ cachedHits;
                factors = lastFactors.clone();
            }
        }
        if (factors == null) {
            factors = factor(bigInteger);
            synchronized (this) {
                lastNumber = bigInteger;
                lastFactors = factors.clone();
            }
        }
        encodeIntoResp(servletResponse, factors);
    }

    private void encodeIntoResp(ServletResponse servletResponse, BigInteger[] factors) {
    }

    private BigInteger[] factor(BigInteger i) {
        System.out.println(i);
        return null;
    }

    private BigInteger extractFromReq(ServletRequest servletRequest) {
        Object bigint = servletRequest.getAttribute("bigint");
        System.out.println(bigint);
        return BigInteger.TEN;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
