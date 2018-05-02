package com.sumerge.gateway.routing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.Service;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

@ApplicationScoped
public class ServiceDiscoveryFilter extends ZuulFilter {

    @Inject
    private Logger log;

    @Inject
    @ConfigProperty(name = "registry.url")
    private String registryUrl;

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public boolean shouldFilter() {
        return "/ms".equals(RequestContext.getCurrentContext().getRequest().getServletPath());
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        String serviceName = context.getRequest().getPathInfo().substring(1);
        serviceName = serviceName.substring(0, serviceName.indexOf('/'));

        try {
            context.setRouteHost(new URL(getHost(serviceName)));
        } catch (MalformedURLException ex) {
            log.error(null, ex);
        }

        // sets custom header to send to the origin
        context.addOriginResponseHeader("cache-control", "max-age=3600");
        return null;
    }


    private String getHost(String serviceName) {
        log.info("GATEWAY :::::::: Getting service host : for service with name : {}", serviceName);
        Service service;
        try {
            Consul consul = Consul.builder().withUrl(registryUrl).build();
            service = consul.agentClient().getServices().get(serviceName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        log.info("GATEWAY :::::::: service address found ::::: {}:{}", service.getAddress(), service.getPort());
        return service.getAddress() + ":" + service.getPort();
    }

}
