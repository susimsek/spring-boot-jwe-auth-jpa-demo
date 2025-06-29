package io.github.susimsek.springbootjweauthjpademo.mapper;

import io.github.susimsek.springbootjweauthjpademo.dto.response.TokenDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpResultDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.security.UserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface MfaMapper {


  @Mapping(target="mfaVerified", source="verified")
  @Mapping(target="accessToken", source="accessToken")
  @Mapping(target="refreshToken", source="refreshToken")
  @Mapping(target="profile", source="user")
  VerifyTotpDTO toVerifyTotpDto(boolean verified, TokenDTO accessToken,
                                TokenDTO refreshToken, User user);

  @Mapping(target = "profile", source = "principal")
  @Mapping(target = "accessToken", source = "accessToken")
  @Mapping(target = "refreshToken", source = "refreshToken")
  VerifyTotpResultDTO toVerifyTotpResultDTO(UserPrincipal principal,
                                            TokenDTO accessToken,
                                            TokenDTO refreshToken);
}
