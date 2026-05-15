package com.bspq26e8.backend.user.repository;

import com.bspq26e8.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query(
        value = "SELECT l.id AS id, l.name AS name, l.code AS code " +
                "FROM user_preferred_languages upl " +
                "JOIN languages l ON l.id = upl.language_id " +
                "WHERE upl.user_id = :userId " +
                "ORDER BY l.name",
        nativeQuery = true
    )
    List<LanguageProjection> findPreferredLanguagesByUserId(@Param("userId") UUID userId);

    interface LanguageProjection {
        Long getId();
        String getName();
        String getCode();
    }
}
