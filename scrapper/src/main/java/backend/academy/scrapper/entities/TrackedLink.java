package backend.academy.scrapper.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors
public class TrackedLink {
    @Id
    @GeneratedValue
    long id;
    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    String url;
    String monitoringService;
}
