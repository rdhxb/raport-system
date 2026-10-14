package rdhxb.raportservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rdhxb.raportservice.service.RaportService;


@RestController
@RequestMapping("/krs")
@RequiredArgsConstructor
public class RaportController {

    private final RaportService service;

    @PostMapping("/send/{krs}")
    public void sendKrsNumber(@PathVariable String krs){
        service.sendKrsToExchanger(krs);
    }

}
