package it.mauiroma.probe;

import it.mauiroma.dto.MovieRepository;
import it.mauiroma.kafka.Movie;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@Liveness
@ApplicationScoped
public class LivenessProbe implements HealthCheck {

    @Inject
    MovieRepository movieRepository;

    @Override
    public HealthCheckResponse call() {
        Movie movie = movieRepository.load("liveness");
        try {
            if (movie == null) {
                return HealthCheckResponse.up("Liveness probe ok");
            } else {
                return HealthCheckResponse.down("Liveness probe ko, Database outline");
            }
        }catch(Exception ex){
            return HealthCheckResponse.down("Liveness probe ko, Database outline "+ex.getMessage());
        }
    }
}
