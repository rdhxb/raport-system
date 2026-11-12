package rdhxb.raportservice.service;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import rdhxb.common.base.Source;
import rdhxb.common.ceidg.CeidgData;
import rdhxb.common.crbr.CrbrData;
import rdhxb.common.krs.KrsData;
import rdhxb.common.nip.WlData;
import rdhxb.common.sudop.SudopData;
import rdhxb.raportservice.dto.RaportDto;
import rdhxb.raportservice.dto.SourceRaportDto;

import rdhxb.raportservice.model.SourceStatus;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Service
public class RaportPdfService {

    private final TemplateEngine engine;
    private final ObjectMapper mapper;
    private final RaportService raportService;

    private Map<Source, Object> dataObjects(UUID requestId){
        RaportDto raportDto = raportService.getRaport(requestId);
        List<SourceRaportDto> raportSourceList = raportDto.raportSources();

        Map<Source, Object> convertedMap = new HashMap<>();

        for (var raportSource : raportSourceList){
            var payloadConverted = convert(raportSource);
            convertedMap.put(raportSource.source(), payloadConverted);
        }
        return convertedMap;

    }
    private Object convert(SourceRaportDto sourceRaportDto){
        Source source = sourceRaportDto.source();
        if (sourceRaportDto.status() == SourceStatus.SUCCESS){
            switch (source){
                case KRS -> {
                    return mapper.readValue(sourceRaportDto.payload(), KrsData.class);
                }
                case WL -> {
                    return mapper.readValue(sourceRaportDto.payload(), WlData.class);
                }
                case CRBR -> {
                    return mapper.readValue(sourceRaportDto.payload(), CrbrData.class);
                }
                case CEIDG -> {
                    return mapper.readValue(sourceRaportDto.payload(), CeidgData.class);
                }
                case SUDOP -> {
                    return mapper.readValue(sourceRaportDto.payload(), SudopData.class);
                }
                case null, default -> throw new IllegalArgumentException("Source null or source does not match any !");
            }
        }else {
            return null;
        }

    }

    private String buildContext(UUID requestId){
        Context context = new Context();

        Map<Source, Object> sourceObjectMap = dataObjects(requestId);

        for (var entry : sourceObjectMap.entrySet()){
            String k = entry.getKey().toString().toLowerCase();
            Object v = entry.getValue();
            context.setVariable(k,v);
        }
        return engine.process("raport", context);
    }

    private byte[] renderPdf(String html){
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        PdfRendererBuilder rendererBuilder = new PdfRendererBuilder();

        rendererBuilder.useFont(() -> getClass().getResourceAsStream("/fonts/IBMPlexSans-Bold.ttf"),"IBM Plex Sans",700, BaseRendererBuilder.FontStyle.NORMAL,true);
        rendererBuilder.useFont(() -> getClass().getResourceAsStream("/fonts/IBMPlexSans-Regular.ttf"),"IBM Plex Sans", 400, BaseRendererBuilder.FontStyle.NORMAL,true);

        rendererBuilder.withHtmlContent(html,null);
        try {
            rendererBuilder.toStream(arrayOutputStream).run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return arrayOutputStream.toByteArray();
    }

    public byte[] generate(UUID requestId){
        String context = buildContext(requestId);

        return renderPdf(context);
    }






}
