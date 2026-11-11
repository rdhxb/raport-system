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

    @PostMapping("/krs/{krs}")
    public UUID sendKrsNumber(@PathVariable String krs){
        return service.sendKrsToExchanger(krs);
    }
    @PostMapping("/nip/{nip}")
    public UUID sendNipToExchanger(@PathVariable String nip){
        return service.sendNipToExchangerDirectly(nip);
    }

    @GetMapping("/{requestId}")
    public RaportDto getRaport(@PathVariable UUID requestId){
        return service.getRaport(requestId);
    }

    @GetMapping("/{requestId}/sudop/{nip}")
    public void getSudop(@PathVariable UUID requestId, @PathVariable String nip){
        System.out.println("Controller for sudop !");
        service.requestSudop(nip,requestId);
    }

}
