package com.api.frontendmeet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.frontendmeet.Entity.RefreshToken;
import com.api.frontendmeet.Entity.UserEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	@Override
	Optional<RefreshToken> findById(Long id);

	Optional<RefreshToken> findByToken(String token);

	int deleteByUser(UserEntity userEntity);

}
