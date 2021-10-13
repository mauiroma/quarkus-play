package it.mauiroma.utils;

import it.mauiroma.pojo.Movie;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
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
