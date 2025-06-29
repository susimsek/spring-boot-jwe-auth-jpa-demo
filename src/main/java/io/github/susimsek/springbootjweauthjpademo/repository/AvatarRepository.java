package io.github.susimsek.springbootjweauthjpademo.repository;

import io.github.susimsek.springbootjweauthjpademo.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, String> {

}
