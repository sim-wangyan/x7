package x7.demo.repository;

import io.xream.sqli.api.BaseRepository;
import org.springframework.stereotype.Repository;
import x7.demo.entity.DogTest;

@Repository
public interface DogTestRepository extends BaseRepository<DogTest> {

}
