package textrepositoryapp;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ComputationResultRepository extends MongoRepository<ComputationResult, String> {
    List<ComputationResult> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
}
