package com.alibaba.fliggy.orcas.concurrent.cpt2;

import javax.servlet.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @description:
 * @date: 2023/10/25 22:43
 * @author: dinglie
 */
public class UnsafeCachingFactorizer implements Servlet {
    private final AtomicReference<BigInteger> lastNumber = new AtomicReference<>();

    private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<>();

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
        if (bigInteger.equals(lastNumber.get())) {
            encodeIntoResp(servletResponse, lastFactors.get());
        } else {
            BigInteger i = extractFromReq(servletRequest);
            BigInteger[] factors = factor(i);
            encodeIntoResp(servletResponse, factors);
        }
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

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

    private void encodeIntoResp(ServletResponse servletResponse, BigInteger[] factors) {
    }

}
