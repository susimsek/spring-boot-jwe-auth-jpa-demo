package io.github.susimsek.springbootjweauthjpademo.mapper;

import io.github.susimsek.springbootjweauthjpademo.dto.response.AuthorityDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface AuthorityMapper {

    AuthorityDTO toAuthorityDto(Authority authority);

    Authority toEntity(CreateAuthorityRequestDTO req);

    default GrantedAuthority toGrantedAuthority(UserAuthorityMapping m) {
        return new SimpleGrantedAuthority(m.getAuthority().getName());
    }

    List<GrantedAuthority> toGrantedAuthorities(Set<UserAuthorityMapping> mappings);

    default List<String> toAuthorityNames(Collection<? extends GrantedAuthority> auths) {
        return auths.stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }

    default List<String> toAuthorityNames(Set<UserAuthorityMapping> mappings) {
        return mappings.stream()
            .map(m -> m.getAuthority().getName())
            .toList();
    }

    void updateAuthority(UpdateAuthorityRequestDTO dto, @MappingTarget Authority entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(PartialUpdateAuthorityRequestDTO dto, @MappingTarget Authority entity);


}
