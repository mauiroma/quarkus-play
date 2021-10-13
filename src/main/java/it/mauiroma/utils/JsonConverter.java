package it.mauiroma.utils;

import io.smallrye.reactive.messaging.kafka.Record;
import it.mauiroma.kafka.Movie;
import it.mauiroma.kafka.MovieProducer;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@ApplicationScoped
@Traced
public class JsonConverter {


    public Movie convertFromString(String json){
        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.fromJson(json, Movie.class);
    }

}
