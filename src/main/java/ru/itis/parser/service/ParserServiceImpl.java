package ru.itis.parser.service;

import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.itis.parser.client.CitiesRestClient;
import ru.itis.parser.entity.Hotel;
import ru.itis.parser.model.MultiComplete;
import ru.itis.parser.model.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ParserServiceImpl implements ParserService {

    private final CitiesRestClient citiesRestClient;
    private final WebClient client;
    private final HotelService hotelService;

    public ParserServiceImpl(@RestClient CitiesRestClient citiesRestClient, WebClient client, HotelService hotelService) {
        this.citiesRestClient = citiesRestClient;
        this.client = client;
        this.hotelService = hotelService;
    }

    @ConfigProperty(name = "ostrovok.hotels.uri.pattern")
    String uriPattern;
    @ConfigProperty(name = "ostrovok.hotels.cookie")
    String cookieValue;
    @ConfigProperty(name = "ostrovok.hotels.accept-language")
    String acceptLanguage;
    @ConfigProperty(name = "ostrovok.hotels.selectors.pagination-class-prefix")
    String paginationClassPrefix;
    @ConfigProperty(name = "ostrovok.hotels.selectors.hotel-cards-class-prefix")
    String hotelCardsPrefix;
    @ConfigProperty(name = "ostrovok.hotels.selectors.hotel-title-class-prefix")
    String hotelTitlePrefix;
    @ConfigProperty(name = "ostrovok.hotels.selectors.hotel-address-class-prefix")
    String hotelAddressPrefix;
    @ConfigProperty(name = "ostrovok.hotels.selectors.hotel-price-class-prefix")
    String hotelPricePrefix;
    @ConfigProperty(name = "ostrovok.hotels.selectors.hotel-rating-class-prefix")
    String hotelRatingPrefix;
    @ConfigProperty(name = "ostrovok.hotels.selectors.hotel-count-of-start-class-prefix")
    String hotelCountOfStartPrefix;

    private int extractCountOfPages(String body) {
        Document doc = Jsoup.parse(body);
        Elements elements = doc.select("[class^=\"%s\"]".formatted(paginationClassPrefix));
        if (elements.isEmpty()) {
            return 1;
        }
        return Integer.parseInt(elements.last().text());
    }

    private List<Hotel> extractHotels(String body) {
        Document doc = Jsoup.parse(body);
        List<Hotel> hotels = new ArrayList<>(20);
        Elements hotelCards = doc.select("[class^=\"%s\"]".formatted(hotelCardsPrefix));
        for (Element hotelCard : hotelCards) {
            Element title = hotelCard.selectFirst("[class^=\"%s\"]".formatted(hotelTitlePrefix));
            String name = title.attribute("title").getValue();
            String href = title.selectFirst("a").attribute("href").getValue();
            String address = hotelCard.selectFirst("[class^=\"%s\"]".formatted(hotelAddressPrefix)).text();
            Integer pricePerNight = Integer.parseInt(hotelCard.selectFirst("[class^=\"%s\"]".formatted(hotelPricePrefix)).text().replaceAll("[^0-9]", ""));
            Double rating = null;
            if (!hotelCard.select("[class^=\"%s\"]".formatted(hotelRatingPrefix)).isEmpty()) {
                rating = Double.parseDouble(hotelCard.selectFirst("[class^=\"%s\"]".formatted(hotelRatingPrefix)).text().replace(",", "."));
            }
            String regex = "mid(\\d+)/";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(href);

            if (!matcher.find()) {
                throw new InternalServerErrorException("Hotel id not found on page");
            }
            Long ostrovokId = Long.parseLong(matcher.group(1));
            Integer countOfStars = hotelCard.select("[class^=\"%s\"]".formatted(hotelCountOfStartPrefix)).size();
            hotels.add(new Hotel(null, ostrovokId, name, address, countOfStars, pricePerNight, rating, href, null));
        }
        return hotels;
    }

    @Override
    public void parseHotelsByCity(String queryCity) {
        MultiComplete multiComplete = citiesRestClient.searchCities(queryCity);
        Region region = multiComplete.getRegions()
                .stream()
                .filter(reg -> "City".equalsIgnoreCase(reg.getType()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("City with name: " + queryCity + " not found"));
        String slug = region.getSlug();
        HttpResponse<String> resp = getHotelsAsync(slug, 1).join();
        int countOfPages = extractCountOfPages(resp.body());
        List<Hotel> hotels = extractHotels(resp.body());
        List<CompletableFuture<List<Hotel>>> futures = new ArrayList<>(countOfPages - 1);
        for (int i = 2; i <= countOfPages; ++i) {
            futures.add(
                    getHotelsAsync(slug, i)
                            .thenApply(HttpResponse::body)
                            .thenApply(this::extractHotels)
            );
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        hotels.addAll(futures.stream()
                .filter(CompletableFuture::isDone)
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList());
        hotelService.upsertHotels(hotels, region);
    }

    private CompletableFuture<HttpResponse<String>> getHotelsAsync(String slug, int page) {
        return client.getAbs(String.format(uriPattern, slug, page))
                .putHeader("Cookie", cookieValue)
                .putHeader("Accept-Language", acceptLanguage)
                .as(BodyCodec.string())
                .send()
                .toCompletionStage()
                .toCompletableFuture().exceptionally((ex) -> {
                    throw new InternalServerErrorException(ex.getMessage());
                });
    }
}
