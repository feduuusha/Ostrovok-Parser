package ru.itis.parser.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.itis.parser.client.CitiesRestClient;
import ru.itis.parser.model.MultiComplete;

import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class ParserServiceImpl implements ParserService {

    private final CitiesRestClient citiesRestClient;
    private final WebDriver driver;

    public ParserServiceImpl(@RestClient CitiesRestClient citiesRestClient, WebDriver driver) {
        this.citiesRestClient = citiesRestClient;
        this.driver = driver;
    }

    @Override
    public void parseHotelsByCity(String city) {
        MultiComplete multiComplete = citiesRestClient.searchCities(city);
        String slug = multiComplete.getRegions()
                .stream()
                .filter(reg -> "City".equalsIgnoreCase(reg.getType()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("City with name: " + city + " not found"))
                .getSlug();
        driver.get("https://ostrovok.ru/hotel/"+ slug +"/");
        List<WebElement> elements = driver.findElements(By.cssSelector("[class^=\"PopupSearchForm_close\"]"));
        if (!elements.isEmpty()) {
            elements.get(0).click();
        }
        elements = driver.findElements(By.cssSelector("[class^=\"HotelCard_ratePriceValue\"]"));
        elements.forEach(element -> System.out.println(element.getText()));
    }
}
