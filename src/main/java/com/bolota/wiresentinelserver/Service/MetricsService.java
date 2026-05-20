package com.bolota.wiresentinelserver.Service;

import com.bolota.wiresentinelserver.Entities.PacketEntity;
import com.bolota.wiresentinelserver.Entities.SystemEntity;
import com.bolota.wiresentinelserver.Entities.UserEntity;
import com.bolota.wiresentinelserver.Resource.PacketEntityRepository;
import com.bolota.wiresentinelserver.Resource.SystemEntityRepository;
import com.bolota.wiresentinelserver.Resource.UserEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MetricsService {
    SystemEntityRepository ser;
    UserEntityRepository uer;
    PasswordEncoder passwordEncoder;
    PacketEntityRepository per;
    public MetricsService(UserEntityRepository uer, PasswordEncoder passwordEncoder, SystemEntityRepository ser, PacketEntityRepository per){
        this.uer = uer;
        this.passwordEncoder = passwordEncoder;
        this.ser = ser;
        this.per = per;
    }
    public ResponseEntity<Page<SystemEntity>> checkJwtSystems(Jwt jwt, Pageable pageable){
        if(jwt == null) return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        String login = jwt.getSubject();
        if(login == null) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UserEntity ue = uer.getByLogin(login);
        if(ue == null) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        ArrayList<SystemEntity> seList = new ArrayList<>();
        ue.getUuids().forEach(i->{
            seList.add(ser.getByUuid(i));
        });
        return new ResponseEntity<>(toPage(seList, pageable),HttpStatusCode.valueOf(200));
    }
    public ResponseEntity<Page<PacketEntity>> checkJwtPackets(Jwt jwt, String uuidString, Pageable pageable){
        if(jwt == null) return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        String login = jwt.getSubject();
        if(login == null || uuidString == null) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UserEntity ue = uer.getByLogin(login);
        if(ue == null) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        UUID uuid;
        try{
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        if(!ser.existsByUuid(uuid)) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        if(!ue.getUuids().contains(uuid)) return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        return new ResponseEntity<>(per.findByUuid(uuid, pageable),HttpStatusCode.valueOf(200));
    }
    public static <T> Page<T> toPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        if (start > end) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
}
