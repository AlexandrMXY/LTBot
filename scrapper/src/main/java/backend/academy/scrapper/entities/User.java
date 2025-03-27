package backend.academy.scrapper.entities;

import backend.academy.scrapper.util.StringListConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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

    @Id
    private long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<TrackedLink> links;

    @Convert(converter = StringListConverter.class)
    @Column(length = 1024)
    private List<String> inactiveTags;


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
}
