package backend.academy.scrapper.entities;

import backend.academy.scrapper.util.StringListConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TrackedLink {
    @Id
    @GeneratedValue
    private long id;

    private String url;
    private String monitoringService;

    @Convert(converter = StringListConverter.class)
    private List<String> tags;
}
