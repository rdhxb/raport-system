package rdhxb.financeservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import rdhxb.financeservice.service.FinanceService;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService service;

    @PostMapping("/upload")
    public String uploadXmlFile(@RequestParam("file") MultipartFile file) throws IOException, ParserConfigurationException, SAXException {

        if (file.isEmpty()){
            return "fAIL TO UPLOAD !";
        }

        // obsluga bledow

        return service.saveData(file);

    }
}
