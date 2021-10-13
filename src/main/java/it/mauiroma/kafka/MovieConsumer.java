package it.mauiroma.kafka;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.kafka.Record;
import it.mauiroma.dto.MovieRepository;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.transaction.Transactional;

@Traced
@ApplicationScoped
public class MovieConsumer {

    private final Logger logger = Logger.getLogger(MovieConsumer.class);

    @Inject
    MovieRepository movieRepository;

    @Incoming("channel-in")
    @Blocking
    @Transactional
    public void receive(Record<Integer, String> record) {
        logger.infof("Got a movie: %d - %s", record.key(), record.value());
        try{
            Jsonb jsonb = JsonbBuilder.create();
            Movie movie = jsonb.fromJson(record.value(), Movie.class);
            movieRepository.persist(movie);
        }catch(Exception ex){
            ex.printStackTrace();
            logger.infof("Discard Message");
        }
    }
}