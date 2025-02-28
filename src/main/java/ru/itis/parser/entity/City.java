package ru.itis.parser.entity;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {
    private Long id;
    private String name;
    private Long countryId;
    private String slug;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof City city)) return false;
        return Objects.equals(id, city.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
