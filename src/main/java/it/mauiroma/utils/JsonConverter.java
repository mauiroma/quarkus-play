package it.mauiroma.utils;

import io.smallrye.reactive.messaging.kafka.Record;
import it.mauiroma.kafka.Movie;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

@ApplicationScoped
@Traced
public class JsonConverter {

    public Movie convertFromKafka(Record<Integer, String> record){
        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.fromJson(record.value(), Movie.class);
    }
}
