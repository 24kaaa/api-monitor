package gelasimov.dev.apimonitor.repository;

import gelasimov.dev.apimonitor.entity.Api;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiRepository extends JpaRepository<Api, UUID> {
    Optional<Api> findByUrl(String url);

    List<Api> findByActiveTrue();
}
