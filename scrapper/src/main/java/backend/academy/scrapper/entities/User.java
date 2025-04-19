package backend.academy.scrapper.entities;

import backend.academy.scrapper.util.StringListConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.ListUtils;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {
    public User(long id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public User(long id, List<TrackedLink> links) {
        this(id, links, new ArrayList<>());
    }

    public User(long id, List<TrackedLink> links, List<String> inactiveTags) {
        this(id, links, inactiveTags, NotificationStrategy.INSTANT, 0);
    }

    @Id
    private long id;

    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @ToString.Exclude
    private List<TrackedLink> links;

    @Convert(converter = StringListConverter.class)
    @Column(length = 1024, name = "inactive_tags")
    private List<String> inactiveTags;

    @Column(name = "notification_policy")
    @Enumerated(EnumType.STRING)
    private NotificationStrategy notificationStrategy;

    @Column(name = "notification_time")
    private int notificationTime;

    public List<TrackedLink> links() {
        return links;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && ListUtils.isEqualList(links, user.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, links);
    }

    public enum NotificationStrategy {
        INSTANT,
        DELAYED;

        public static NotificationStrategy fromString(String policy) {
            return switch (policy) {
                case "INSTANT" -> INSTANT;
                case "DELAYED" -> DELAYED;
                default -> throw new IllegalArgumentException("Unknown notification strategy: " + policy);
            };
        }
    }
}
