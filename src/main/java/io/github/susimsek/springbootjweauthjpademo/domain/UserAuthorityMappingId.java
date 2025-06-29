package io.github.susimsek.springbootjweauthjpademo.domain;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorityMappingId implements Serializable {

    private String userId;
    private String authorityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAuthorityMappingId that = (UserAuthorityMappingId) o;
        return Objects.equals(userId, that.userId) &&
            Objects.equals(authorityId, that.authorityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, authorityId);
    }
}
