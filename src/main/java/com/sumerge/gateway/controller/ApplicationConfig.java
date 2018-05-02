package com.sumerge.gateway.controller;

import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.sumerge.gateway.metrics.MetricsConfigurer;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("resources")
public class ApplicationConfig extends Application {

    @Inject
    private MetricsConfigurer metricsConfigurer;

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);
        addRestResourceClasses(resources);
        return resources;
    }


    @Override
    public Set<Object> getSingletons() {
        final Set<Object> instances = new HashSet<>();
        instances.add(new InstrumentedResourceMethodApplicationListener(metricsConfigurer.getMetricRegistry()));
        return instances;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.sumerge.gateway.controller.LogsResource.class);
        resources.add(com.sumerge.gateway.metrics.DiagnosticFilter.class);
        resources.add(com.sumerge.gateway.web.CORSFilter.class);
        resources.add(InstrumentedResourceMethodApplicationListener.class);
    }
}
