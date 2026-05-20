package com.bolota.wiresentinelserver.Service;

import com.bolota.wiresentinelserver.Resource.PacketEntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CleaningService {
    PacketEntityRepository per;
    public CleaningService(PacketEntityRepository per){
        this.per = per;
    }
    @Transactional
    @Scheduled(cron ="0 0 0 * * *")
    public void cleanup(){
        LocalDateTime limit = LocalDateTime.now().minusDays(3);
        per.deleteByTimestampBefore(limit);
    }
}
