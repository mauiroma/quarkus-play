package it.mauiroma.dto;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import it.mauiroma.kafka.Movie;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
@Traced
public class MovieRepository implements PanacheRepository<Movie> {

    public void persist(Movie movie){
        movie.persistAndFlush();
    }


    public Movie load(String title){
        return Movie.find("title", title).firstResult();
    }

    public List<Movie> loadAll(){
        return Movie.listAll();
    }

}
