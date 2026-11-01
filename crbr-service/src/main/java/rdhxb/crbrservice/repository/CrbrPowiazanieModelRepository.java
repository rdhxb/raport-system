package rdhxb.crbrservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;

import java.util.UUID;

public interface CrbrPowiazanieModelRepository extends JpaRepository<CrbrPowiazanieModel, UUID> {


    void deleteAllByRequestId(UUID requestId);
}
