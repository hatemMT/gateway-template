package com.sumerge.gateway.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.annotation.Timed;
import com.sumerge.gateway.controller.dto.LoggerVM;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for view and managing Log Level at runtime.
 */
@Path("/management")
@Api(value = "Managing logging", description = "Api to show and modify the logs severity")
public class LogsResource {

    @Path("/logs")
    @GET
    @Timed
    @ApiOperation(value = "/logs", httpMethod = "GET")
    @ApiResponse(code = 200, message = "List of logger packages", response = LoggerVM.class)
    public List<LoggerVM> getList() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLoggerList()
                .stream()
                .map(LoggerVM::new)
                .collect(Collectors.toList());
    }

    @Path("/logs")
    @PUT
    @Timed
    public void changeLevel(LoggerVM jsonLogger) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(jsonLogger.getName()).setLevel(Level.valueOf(jsonLogger.getLevel()));
    }
}
