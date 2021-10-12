package it.mauiroma.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.MetricUnits;

@Path("/hello")
public class GreetingResource {


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/greeting/{name}")
    @Counted(description = "How many greetings", absolute = true, name = "countGreeting")
    @Timed(name = "timerGreeting", description = "A measure of how long it takes to perform", unit = MetricUnits.MILLISECONDS)  
    public String greeting(@PathParam("name") String name) {
        return "hello"+name;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Counted(description = "How many hello", absolute = true, name = "countHello")  
    @Timed(name = "timerHello", description = "A measure of how long it takes to perform", unit = MetricUnits.MILLISECONDS)  
    public String hello() {
        return "hello";
    }
}