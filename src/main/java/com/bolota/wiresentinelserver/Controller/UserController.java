package com.bolota.wiresentinelserver.Controller;

import com.bolota.wiresentinelserver.Entities.UserEntity;
import com.bolota.wiresentinelserver.Resource.SystemEntityRepository;
import com.bolota.wiresentinelserver.Resource.UserEntityRepository;
import com.bolota.wiresentinelserver.Service.GeneralService;
import com.bolota.wiresentinelserver.Service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/user")
public class UserController {
    UserEntityRepository uer;
    SystemEntityRepository ser;
    UserService us;
    PasswordEncoder passwordEncoder;
    JwtEncoder jwtEncoder;
    public UserController(UserEntityRepository uer, PasswordEncoder passwordEncoder, SystemEntityRepository ser, JwtEncoder jwtEncoder, UserService us){
        this.us = us;
        this.ser = ser;
        this.uer = uer;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody HashMap<String,String> login){
        ResponseEntity<String> response = us.checkLoginRegister(login);
        if(response.getStatusCode() != HttpStatusCode.valueOf(200)) return response;
        UserEntity ue = new UserEntity();
        ue.setLogin((String)login.get("login"));
        ue.setPasswordHash(passwordEncoder.encode((String)login.get("password")));
        ue.setUuids(new ArrayList<>());
        uer.save(ue);
        return response;
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody HashMap<String,String> login){
        return  us.checkLoginLogin(login);
    }
    @Transactional
    @PostMapping("/register_uuid/{uuidString}")
    public ResponseEntity<String> registerUuid(@AuthenticationPrincipal Jwt jwt, @PathVariable String uuidString){
        if (uuidString.trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UUID uuid;
        try{
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        if(!ser.existsByUuid(uuid)) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        ResponseEntity<String> response = us.checkJwt(jwt);
        if(response.getStatusCode() != HttpStatusCode.valueOf(200)) return response;
        UserEntity ue = uer.getByLogin((jwt.getSubject()));
        if(ue.getUuids().contains(uuid)) return new ResponseEntity<>(HttpStatusCode.valueOf(409));
        ue.getUuids().add(uuid);
        return response;
    }
    @Transactional
    @PostMapping("/remove_uuid/{uuidString}")
    public ResponseEntity<String> removeUuid(@AuthenticationPrincipal Jwt jwt, @PathVariable String uuidString){
        if (uuidString.trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UUID uuid;
        try{
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        if(!ser.existsByUuid(uuid)) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        ResponseEntity<String> response = us.checkJwt(jwt);
        if(response.getStatusCode() != HttpStatusCode.valueOf(200)) return response;
        UserEntity ue = uer.getByLogin((jwt.getSubject()));
        ue.getUuids().remove(uuid);
        return response;
    }
}
