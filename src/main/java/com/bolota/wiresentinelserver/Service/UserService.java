package com.bolota.wiresentinelserver.Service;

import com.bolota.wiresentinelserver.Entities.UserEntity;
import com.bolota.wiresentinelserver.Resource.UserEntityRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService {
    UserEntityRepository uer;
    JwtEncoder jwtEncoder;
    PasswordEncoder passwordEncoder;
    public UserService(UserEntityRepository uer, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder){
        this.uer = uer;
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
    }
    public ResponseEntity<String> checkLoginRegister(HashMap<String,String> login){
        if(login == null) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.get("login") == null || login.get("password") == null ) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.get("login").trim().isEmpty() || login.get("password").trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(uer.existsByLogin(login.get("login"))) return new ResponseEntity<>(HttpStatusCode.valueOf(409));
        return new ResponseEntity<>(issueLoginToken(login.get("login")),HttpStatusCode.valueOf(200));
    }
    public ResponseEntity<String> checkLoginLogin(HashMap<String,String> login){
        if(login == null) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.get("login") == null || login.get("password") == null ) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.get("login").trim().isEmpty() || login.get("password").trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UserEntity ue = uer.getByLogin(login.get("login"));
        if(ue == null) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        if(!passwordEncoder.matches(login.get("password"),ue.getPasswordHash())) return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        return new ResponseEntity<>(issueLoginToken(ue.getLogin()),HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<String> checkJwt(Jwt jwt){
        if(jwt == null) return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        String login = jwt.getSubject();
        if(login == null) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        if(login.trim().isEmpty()) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UserEntity ue = uer.getByLogin(login);
        if(ue == null) return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
    public String issueLoginToken(String login) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("SysSentinelHost")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60L * 60L * 3L))
                .subject(login)
                .claim("roles", List.of("USER"))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue();
    }
}
