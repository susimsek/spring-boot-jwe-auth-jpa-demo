package io.github.susimsek.springbootjweauthjpademo.dto.filter;

import io.github.susimsek.springbootjweauthjpademo.domain.Message;
import io.github.susimsek.springbootjweauthjpademo.domain.Message_;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public record MessageFilter(
    @Parameter(description = "Search term")
    @Size(min = 1, max = 100)
    String search,

    @Parameter(description = "Locale filter")
    @Size(min = 2, max = 10)
    String locale
) {
    public Specification<Message> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get(Message_.code)), like),
                    cb.like(cb.lower(root.get(Message_.content)), like)
                ));
            }
            if (StringUtils.hasText(locale)) {
                predicates.add(cb.equal(root.get(Message_.locale), locale));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
