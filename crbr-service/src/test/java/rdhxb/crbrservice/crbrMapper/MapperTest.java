package rdhxb.crbrservice.crbrMapper;

import org.junit.jupiter.api.Test;
import rdhxb.crbrservice.dto.CrbrApiResponse.Beneficjent;
import rdhxb.crbrservice.dto.CrbrApiResponse.Spolka;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperTest {

    private final Mapper mapper = new Mapper();

    @Test
    public void shouldKeepBothBeneficiariesWhenMaskedPeselsCollide(){
        // given - dwie rozne osoby z tym samym prefiksem PESEL (90010)
        String krs = "0000123456";
        Spolka spolka = spolka(krs, beneficjent("Jan", "90010112345"), beneficjent("Anna", "90010554321"));

        // when
        Map<String, CrbrPowiazanieModel> powiazania = mapper.toPowiazania(spolka, UUID.randomUUID(), krs);

        // then
        assertThat(powiazania).hasSize(2);
        assertThat(powiazania.values())
                .extracting(CrbrPowiazanieModel::getImie)
                .containsExactlyInAnyOrder("Jan", "Anna");
    }

    @Test
    public void shouldStoreOnlyMaskedPeselInEntity(){
        // given
        String krs = "0000123456";
        Spolka spolka = spolka(krs, beneficjent("Jan", "90010112345"));

        // when
        Map<String, CrbrPowiazanieModel> powiazania = mapper.toPowiazania(spolka, UUID.randomUUID(), krs);

        // then
        assertThat(powiazania.values())
                .extracting(CrbrPowiazanieModel::getPesel)
                .containsExactly("90010******");
    }

    private static Spolka spolka(String krs, Beneficjent... beneficjenci) {
        return new Spolka(null, null, krs, null, null, null, null, null, null, null,
                List.of(beneficjenci), List.of(), List.of());
    }

    private static Beneficjent beneficjent(String imie, String pesel) {
        return new Beneficjent(imie, null, null, pesel, null, List.of(), null, List.of(), null, null);
    }
}
