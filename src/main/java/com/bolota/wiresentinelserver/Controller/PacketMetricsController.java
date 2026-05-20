package com.bolota.wiresentinelserver.Controller;

import com.bolota.wiresentinelserver.Entities.PacketEntity;
import com.bolota.wiresentinelserver.Entities.SystemEntity;
import com.bolota.wiresentinelserver.Resource.PacketEntityRepository;
import com.bolota.wiresentinelserver.Resource.SystemEntityRepository;
import com.bolota.wiresentinelserver.Resource.UserEntityRepository;
import com.bolota.wiresentinelserver.Service.MetricsService;
import com.bolota.wiresentinelserver.Service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
public class PacketMetricsController {
    SystemEntityRepository ser;
    PacketEntityRepository per;
    UserEntityRepository   uer;
    UserService            us;
    MetricsService         ms;
    public PacketMetricsController(SystemEntityRepository ser, PacketEntityRepository per, UserEntityRepository uer, UserService us, MetricsService ms){
        this.ser = ser;
        this.per = per;
        this.uer = uer;
        this.us = us;
        this.ms = ms;
    }
    @GetMapping("/systems")
    public ResponseEntity<Page<SystemEntity>> systemList(@AuthenticationPrincipal Jwt jwt, @PageableDefault(size = 10) Pageable pageable){
        return ms.checkJwtSystems(jwt, pageable);
    }
    @GetMapping("/system/{uuid}/packets")
    public ResponseEntity<Page<PacketEntity>> packetList(@AuthenticationPrincipal Jwt jwt, @PathVariable String uuid, @PageableDefault(size = 50) Pageable pageable){
        return ms.checkJwtPackets(jwt, uuid ,pageable);
    }
}
