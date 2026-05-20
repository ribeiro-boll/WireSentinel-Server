package com.bolota.wiresentinelserver.Resource;

import com.bolota.wiresentinelserver.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByLogin(String login);
    UserEntity getByLogin(String login);
}
