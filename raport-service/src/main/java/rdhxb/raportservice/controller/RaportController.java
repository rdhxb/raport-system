package rdhxb.raportservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rdhxb.raportservice.dto.RaportDto;
import rdhxb.raportservice.service.RaportService;

import java.util.UUID;


@RestController
@RequestMapping("/raports")
@RequiredArgsConstructor
public class RaportController {

    private final RaportService service;

    @PostMapping("/{krs}")
    public UUID sendKrsNumber(@PathVariable String krs){
        return service.sendKrsToExchanger(krs);
    }

    @GetMapping("/{requestId}")
    public RaportDto getRaport(@PathVariable UUID requestId){
        return service.getRaport(requestId);
    }

}
