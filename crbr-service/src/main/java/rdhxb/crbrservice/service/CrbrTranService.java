package rdhxb.crbrservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;
import rdhxb.crbrservice.repository.CrbrModelRepository;
import rdhxb.crbrservice.repository.CrbrPowiazanieModelRepository;


import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrbrTranService {
    private final CrbrModelRepository crbrModelRepository;
    private final CrbrPowiazanieModelRepository powiazanieModelRepository;

    @Transactional
    public void saveData(CrbrModel crbrModel, Collection<CrbrPowiazanieModel> powiazania){

        var oldData = crbrModelRepository.findByKrsNumber(crbrModel.getKrsNumber());

        if (oldData.isPresent()){
            UUID oldRequestId = oldData.get().getRequestId();
            crbrModel.setId(oldData.get().getId());

            powiazanieModelRepository.deleteAllByRequestId(oldRequestId);
            powiazanieModelRepository.flush();
        }

        crbrModelRepository.save(crbrModel);
        powiazanieModelRepository.saveAll(powiazania);
    }

}
