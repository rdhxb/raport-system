package rdhxb.krspdfservice.scraper;


import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class KrsPdfScraper {

    public void scrapePdf(String krsNumber){
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            page.navigate("https://wyszukiwarka-krs.ms.gov.pl/");

//            input do wpisania krs
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Numer KRS")).fill(krsNumber);

//            przycisk wyszukaj
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Wyszukaj")).click();
//            link wyswietl szczgoly
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Wyświetl szczegóły")).click();

//            nowe okno i tu pobieramy pdf
            Locator pdfButton = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Pobierz PDF")).first();

            Download download = page.waitForDownload(() -> pdfButton.click());

            Path target = Paths.get("data", "KRS_" + krsNumber + ".pdf");
            download.saveAs(target);
            log.info("Scraped pdf !");

            browser.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
