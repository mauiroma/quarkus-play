package it.mauiroma.rest;
import com.github.javafaker.Faker;
import it.mauiroma.dto.MovieRepository;
import it.mauiroma.kafka.Movie;
import it.mauiroma.kafka.MovieProducer;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.SseElementType;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/movie")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovieResource {

    private final Logger logger = Logger.getLogger(MovieResource.class);


    @Inject
    MovieProducer producer;

    @Inject
    MovieRepository movieRepository;

    @POST
    @Path("/add/post")
    public Response createJava(@FormParam("json") String json) {
        Jsonb jsonb = JsonbBuilder.create();
        Movie movie = jsonb.fromJson(json, Movie.class);
        producer.sendMovieToKafka(movie);
        return Response.ok(movie).build();
    }

    @POST
    @Path("/add/postJson")
    public Response createJson(@FormParam("title") String title, @FormParam("year") int year) {
        Movie movie = new Movie(title, year);
        producer.sendMovieToKafka(movie);
        return Response.ok(movie).build();
    }

    @POST
    @Path("/add")
    @Counted(description = "How many addMovie", absolute = true, name = "countAddMovie")
    @Timed(name = "timerAddMovie", description = "A measure of how long it takes to perform", unit = MetricUnits.MILLISECONDS)
    public Response add(Movie movie) {
        logger.infof("add new movie %s", movie);
        producer.sendMovieToKafka(movie);
        return Response.ok(movie).build();
    }

    @GET
    @Path("/addRandom")
    @Counted(description = "How many add Random Movie", absolute = true, name = "countAddRanomMovie")
    @Timed(name = "timerAddRandomMovie", description = "A measure of how long it takes to perform", unit = MetricUnits.MILLISECONDS)
    public Response add() {
        Faker faker = new Faker();
        Movie movie = new Movie(faker.artist().name(), 2021);
        logger.infof("add random movie %s", movie);
        producer.sendMovieToKafka(movie);
        return Response.ok(movie).build();
    }

    @GET
    @Path("/get/{title}")
    public Response get(@PathParam("title") String title) {
        logger.infof("read Movie by title %s", title);
        return Response.status(Response.Status.OK).entity(movieRepository.load(title)).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAll() {
        logger.infof("read all Movie");
        Jsonb jsonb = JsonbBuilder.create();
        String result = jsonb.toJson(movieRepository.loadAll());
        return Response.status(Response.Status.OK).entity(result).build();
    }
}