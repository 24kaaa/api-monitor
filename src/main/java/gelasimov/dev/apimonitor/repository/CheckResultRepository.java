package gelasimov.dev.apimonitor.repository;

import gelasimov.dev.apimonitor.entity.CheckResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CheckResultRepository extends JpaRepository<CheckResult, UUID> {

    Page<CheckResult> findByApi_IdOrderByCheckedAtDesc(UUID id, Pageable pageable);

    @Modifying
    @Query("DELETE FROM CheckResult c WHERE c.checkedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
