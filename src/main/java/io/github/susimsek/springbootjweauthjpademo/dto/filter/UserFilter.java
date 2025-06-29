package io.github.susimsek.springbootjweauthjpademo.dto.filter;

import io.github.susimsek.springbootjweauthjpademo.domain.Authority_;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping_;
import io.github.susimsek.springbootjweauthjpademo.domain.User_;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.AuthorityName;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public record UserFilter(
    @Parameter(description = "Search term")
    @Size(min = 1, max = 100)
    String search,

    @Parameter(description = "Authority name to filter by")
    @Size(min = 3, max = 50)
    @AuthorityName
    String authority,

    @Parameter(description = "Enabled status filter")
    Boolean enabled,

    @Parameter(description = "MFA enabled filter")
    Boolean mfaEnabled,

    @Parameter(description = "Protected status filter")
    Boolean protectedFlag,

    @Parameter(description = "Locked status filter")
    Boolean locked
) {

    public Specification<User> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get(User_.username)), like),
                    cb.like(cb.lower(root.get(User_.firstName)), like),
                    cb.like(cb.lower(root.get(User_.lastName)), like)
                ));
            }
            if (StringUtils.hasText(authority)) {
                Join<User, UserAuthorityMapping> jam = root.join(User_.authorities, JoinType.INNER);
                predicates.add(cb.equal(jam.get(UserAuthorityMapping_.authority).get(Authority_.name), authority));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get(User_.enabled), enabled));
            }
            if (mfaEnabled != null) {
                predicates.add(cb.equal(root.get(User_.mfaEnabled), mfaEnabled));
            }
            if (protectedFlag != null) {
                predicates.add(cb.equal(root.get(User_.protectedFlag), protectedFlag));
            }
            if (locked != null) {
                predicates.add(cb.equal(root.get(User_.locked), locked));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
