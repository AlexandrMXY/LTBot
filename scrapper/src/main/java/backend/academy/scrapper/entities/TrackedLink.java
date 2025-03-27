package backend.academy.scrapper.entities;

import backend.academy.scrapper.util.StringListConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class TrackedLink {
    @Id
    @GeneratedValue
    @Setter
    private long id;

    @ManyToOne()
    @ToString.Exclude
    private User user;

    private String url;
    private String monitoringService;

    @Convert(converter = StringListConverter.class)
    @Column(length = 1024)
    private List<String> tags;

    @Convert(converter = StringListConverter.class)
    @Column(length = 1024)
    private List<String> filters;

    private String serviceId;

    private long lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackedLink link = (TrackedLink) o;
        return  id == link.id &&
                (user == null ? link.user == null : user.id() == link.user.id()) &&
                lastUpdate == link.lastUpdate && Objects.equals(url, link.url) &&
                Objects.equals(monitoringService, link.monitoringService) &&
                ListUtils.isEqualList(tags, link.tags) &&
                ListUtils.isEqualList(filters, link.filters) &&
                Objects.equals(serviceId, link.serviceId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, monitoringService, tags, filters, serviceId, lastUpdate);
    }
}
