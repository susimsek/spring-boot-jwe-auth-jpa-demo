package io.github.susimsek.springbootjweauthjpademo.repository;

import io.github.susimsek.springbootjweauthjpademo.domain.Message;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String>,JpaSpecificationExecutor<Message> {

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))
    List<Message> findByLocale(String locale);

    boolean existsByCodeAndLocale(String code, String locale);
}
