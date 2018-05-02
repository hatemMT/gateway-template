package com.sumerge.gateway.web;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.MetricsServlet;
import com.sumerge.gateway.metrics.MetricsConfigurer;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@WebListener
public class WebConfigurer implements ServletContextListener {

    @Inject
    private Logger log;

    @Inject
    private MetricsConfigurer metricsConfigurer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
        initMetrics(sce.getServletContext(), disps);
        initDocs(sce.getServletContext());
        log.info("Web application fully configured");
    }

    /**
     * Initializes Metrics.
     */
    private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        MetricRegistry metricRegistry = metricsConfigurer.getMetricRegistry();
        log.debug("Initializing Metrics registries");
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricRegistry);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);

        log.debug("Registering Metrics Filter");
        FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("webappMetricsFilter",
                new InstrumentedFilter());

        metricsFilter.addMappingForUrlPatterns(disps, true, "/*");
        metricsFilter.setAsyncSupported(true);

        log.debug("Registering Metrics Servlet");
        ServletRegistration.Dynamic metricsAdminServlet
                = servletContext.addServlet("metricsServlet", new MetricsServlet());

        metricsAdminServlet.addMapping("/management/metrics/*");
        metricsAdminServlet.setAsyncSupported(true);
        metricsAdminServlet.setLoadOnStartup(2);

    }

    /**
     * Initializes Swagger.
     */
    private void initDocs(ServletContext servletContext) {
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        ConfigFactory.setConfig(swaggerConfig);
        String contextPath = servletContext.getContextPath();
        log.info("Context path for swagger : {}", contextPath);
        swaggerConfig.setBasePath(contextPath + "/resources");
        swaggerConfig.setApiVersion("1.0.0");
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
