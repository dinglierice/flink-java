package com.alibaba.fliggy.orcas.concurrent.cpt2;

import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.*;
import java.io.IOException;

/**
 * @description: 线程不安全的servlet
 * @date: 2023/10/25 21:14
 * @author: dinglie
 */
@NotThreadSafe
public class UnsafeCountFactorizer implements Servlet {
    private long count = 0;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        count ++ ;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
