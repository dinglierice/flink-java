package com.alibaba.fliggy.orcas.concurrent.cpt2;

import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.*;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @description:
 * @date: 2023/10/25 00:35
 * @author: dinglie
 */
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
