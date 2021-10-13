package it.mauiroma.kafka;

import io.smallrye.reactive.messaging.kafka.Record;
import it.mauiroma.dto.MovieRepository;
import it.mauiroma.utils.JsonConverter;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.transaction.Transactional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
@Traced
public class MovieProducer {

    @Inject @Channel("channel-out")
    Emitter<Record<Integer, String>> emitter;

    @Inject
    JsonConverter jsonConverter;

    private final Logger logger = Logger.getLogger(MovieProducer.class);

    private AtomicInteger messageCount = new AtomicInteger(0);
    private BlockingQueue<Integer> messages = new LinkedBlockingQueue<Integer>();

    public void sendMovieToKafka(String movie) {
        try {
            logger.infof("Sent %s movie to kafka",movie);
            messages.add(messageCount.incrementAndGet());
            int index = messages.take();
            emitter.send(Record.of(index, movie));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}