package rdhxb.raportservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rdhxb.common.base.Source;
import rdhxb.raportservice.entity.RaportSource;
import rdhxb.raportservice.model.SourceStatus;

import java.util.Optional;
import java.util.UUID;

public interface RaportSourceRepository extends JpaRepository<RaportSource, UUID> {
    Optional<RaportSource> findByRaportRequestIdAndSource(UUID requestId, Source source);

    boolean existsByRaportRequestIdAndStatus(UUID requestId, SourceStatus sourceStatus);
}
