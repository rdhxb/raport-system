package rdhxb.raportservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import rdhxb.raportservice.dto.RaportDto;
import rdhxb.raportservice.service.RaportPdfService;
import rdhxb.raportservice.service.RaportService;

import java.util.UUID;


@RestController
@RequestMapping("/raports")
@RequiredArgsConstructor
public class RaportController {

    private final RaportService service;
    private final RaportPdfService pdfService;

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

    @PostMapping("/{requestId}/sudop")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID sendSudop(@PathVariable UUID requestId){
        return service.requestSudop(requestId);
    }

    @GetMapping("/{requestId}/pdf")
    public ResponseEntity<byte[]> generateRaportPdf(@PathVariable UUID requestId){
        var pdf = pdfService.generate(requestId);

        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("raport-" + requestId + ".pdf").build().toString())
                .body(pdf);

        return responseEntity;
    }

}
