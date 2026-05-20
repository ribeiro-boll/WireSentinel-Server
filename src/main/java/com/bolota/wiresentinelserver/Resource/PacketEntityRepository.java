package com.bolota.wiresentinelserver.Resource;

import com.bolota.wiresentinelserver.Entities.PacketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PacketEntityRepository extends JpaRepository<PacketEntity, Long> {

    boolean existsByUuid(UUID UUID);
    List<PacketEntity> findByUuid(UUID agentId);
    Page<PacketEntity> findByUuid(UUID agentId,Pageable pageable);

    void deleteByTimestampBefore(LocalDateTime timestamp);

}
