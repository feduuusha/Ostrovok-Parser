package ru.itis.parser.entity;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    private Long id;
    private Long ostrovokId;
    private String name;
    private String address;
    private Integer countOfStars;
    private Integer minPricePerNight;
    private Double rating;
    private String href;
    private Long cityId;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Hotel hotel)) return false;
        return Objects.equals(id, hotel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
