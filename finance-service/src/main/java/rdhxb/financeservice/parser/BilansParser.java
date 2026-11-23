package rdhxb.financeservice.parser;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;

import static rdhxb.financeservice.util.XmlUtils.findElement;
import static rdhxb.financeservice.util.XmlUtils.getKwota;

@Component
public class BilansParser {

    public Bilans parse(Document doc) {
        Element bilans = findElement(doc.getDocumentElement(), "Bilans");
        if (bilans == null) {
            return null;
        }

        return new Bilans(
                getKwota(findElement(bilans, "Aktywa")),
                getKwota(findElement(bilans, "Aktywa_A")),
                getKwota(findElement(bilans, "Aktywa_B")),
                getKwota(findElement(bilans, "Aktywa_B_I")),
                getKwota(findElement(bilans, "Aktywa_B_II")),
                getKwota(findElement(bilans, "Aktywa_B_III_1_C_1")),
                getKwota(findElement(bilans, "Aktywa_B_IV")),
                getKwota(findElement(bilans, "Pasywa_A")),
                getKwota(findElement(bilans, "Pasywa_A_I")),
                getKwota(findElement(bilans, "Pasywa_A_II")),
                getKwota(findElement(bilans, "Pasywa_A_V")),
                getKwota(findElement(bilans, "Pasywa_A_VI")),
                getKwota(findElement(bilans, "Pasywa_B")),
                getKwota(findElement(bilans, "Pasywa_B_I")),
                getKwota(findElement(bilans, "Pasywa_B_II")),
                getKwota(findElement(bilans, "Pasywa_B_II_3_A")),
                getKwota(findElement(bilans, "Pasywa_B_III")),
                getKwota(findElement(bilans, "Pasywa_B_III_3_D")),
                getKwota(findElement(bilans, "Pasywa_B_III_3_G")),
                getKwota(findElement(bilans, "Pasywa_B_III_3_H"))
        );
    }
}
