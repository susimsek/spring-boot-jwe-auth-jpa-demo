package io.github.susimsek.springbootjweauthjpademo.dto.filter;

import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.domain.Authority_;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public record AuthorityFilter(
    @Parameter(description = "Search term")
    @Size(min = 1, max = 100)
    String search,

    @Parameter(description = "Protected status filter")
    Boolean protectedFlag
) {
    public Specification<Authority> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get(Authority_.name)), like),
                    cb.like(cb.lower(root.get(Authority_.description)), like)
                ));
            }
            if (protectedFlag != null) {
                predicates.add(cb.equal(root.get(Authority_.protectedFlag), protectedFlag));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
