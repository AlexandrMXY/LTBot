package backend.academy.scrapper.entities;

import backend.academy.scrapper.entities.filters.Filters;
import backend.academy.scrapper.util.FiltersConverter;
import backend.academy.scrapper.util.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.ListUtils;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Table(name = "tracked_link")
public class TrackedLink {
    @Id
    @SequenceGenerator(allocationSize = 1, name = "tracked_link_seq", sequenceName = "tracked_link_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tracked_link_seq")
    @Setter
    private long id;

    @ManyToOne()
    @ToString.Exclude
    private User user;

    private String url;

    @Column(name = "monitoring_service")
    private String monitoringService;

    @Convert(converter = StringListConverter.class)
    @Column(length = 1024)
    private List<String> tags;

    @Convert(converter = FiltersConverter.class)
    @Column(length = 1024)
    private Filters filters;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "last_update")
    private long lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackedLink link = (TrackedLink) o;
        return id == link.id
                && (user == null ? link.user == null : user.id() == link.user.id())
                && lastUpdate == link.lastUpdate
                && Objects.equals(url, link.url)
                && Objects.equals(monitoringService, link.monitoringService)
                && ListUtils.isEqualList(tags, link.tags)
                && Objects.equals(filters, link.filters)
                && Objects.equals(serviceId, link.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, user == null ? null : user.id(), url, monitoringService, tags, filters, serviceId, lastUpdate);
    }
}
