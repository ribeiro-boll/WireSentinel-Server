package com.bolota.wiresentinelserver.Controller;
import com.bolota.wiresentinelserver.Service.GeneralService;
import com.bolota.wiresentinelserver.Entities.PacketBatchEntity;
import com.bolota.wiresentinelserver.Entities.PacketEntity;
import com.bolota.wiresentinelserver.Entities.SystemEntity;
import com.bolota.wiresentinelserver.Resource.PacketEntityRepository;
import com.bolota.wiresentinelserver.Resource.SystemEntityRepository;
import com.bolota.wiresentinelserver.Service.GeneralService;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;


@RestController
@RequestMapping("/api")
public class PacketController {
    PacketEntityRepository per;
    SystemEntityRepository ser;
    public PacketController (PacketEntityRepository per, SystemEntityRepository ser){
        this.per = per;
        this.ser = ser;
    }

    @Value("${wiresentinel.sharedkey}")
    private String secret;

    @PostMapping("/ingest")
    public ResponseEntity<Void> packetIngest(@RequestHeader("Content-Length") Integer lenght,
                             @RequestHeader("X-WireSentinel-Credential") String credential,
                             @RequestHeader("X-WireSentinel-Timestamp") String timestamp,
                             @RequestHeader("X-WireSentinel-UUID") String uuidString,
                             @RequestBody PacketBatchEntity pbe){
        String jsonHash = "{\r\n  X-WireSentinel-Timestamp: "+timestamp +",\r\n  Length: "+lenght+ "\r\n}";
        LocalDateTime date;
        System.out.println("Ingeri 1!!" + uuidString);
        try {
            date = LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        System.out.println("Ingeri 2!!" + uuidString);
        if (date.isBefore(LocalDateTime.now().minusMinutes(3)) || date.isAfter(LocalDateTime.now().plusMinutes(1))) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        UUID uuid;
        System.out.println("Ingeri 3!!" + uuidString);
        try{
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        System.out.println("Ingeri 4!!" + uuidString);
        String hmac = getHmac(jsonHash);
        //System.out.println(credential + "\n" + hmac);
        if (hmac.equals(credential) && ser.existsByUuid(uuid)){
            for(PacketEntity i : pbe.getPackets()){
                i.setUuid(uuid);
            }
            per.saveAll(pbe.getPackets());
            return new ResponseEntity<Void>(HttpStatusCode.valueOf(200));
        }
        return new ResponseEntity<Void>(HttpStatusCode.valueOf(401));
        //ArrayList<PacketEntity> peList = (ArrayList<PacketEntity>) per.findByUuid(uuid);
        //peLis
        //per.findByUuid(uuid).forEach(System.out::println); ;
    }

    @GetMapping("/register")
    public ResponseEntity<String> pcRegister(@RequestHeader("User-Agent") String user, @RequestHeader("X-WireSentinel-Credential") String credential, @RequestHeader("X-WireSentinel-Timestamp") String timestamp){
        int test = 0;
        System.out.println("[AUTH] timestamp=" + timestamp);
        System.out.println("[AUTH] userAgent=" + user);
        System.out.println("[AUTH] credential recebida=" + credential);
        //snprintf(json, 512, "{\r\n  X-WireSentinel-Timestamp: %s,\r\n  Length: %ld\r\n}", time, content_length);
        LocalDateTime date;
        //System.err.println("Teste: "+ test++);
        try {
            //System.err.println("Teste: "+ test++);
            date = LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            //System.err.println("Teste catch: "+ test++);
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        if (date.isBefore(LocalDateTime.now().minusMinutes(3)) || date.isAfter(LocalDateTime.now().plusMinutes(1))){
            //System.err.println( date + "   " + LocalDateTime.now().minusMinutes(3));
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        String jsonHash =
                "{\r\n" +
                "  X-WireSentinel-Timestamp: "+timestamp+",\r\n" +
                "  User-Agent: "+user+"\r\n" +
                "}";
        String algorithm = "HmacSHA256";
        try{
            String hmac = getHmac(jsonHash);
            //System.out.println(jsonHash + "\n" + credential+ "\n" + hmac);
            System.out.println("[AUTH] string assinada=[" + credential + "]");
            System.out.println("[AUTH] hmac gerado=" + hmac);
            if (hmac.equals(credential)){
                UUID uuidGen = UUID.randomUUID();
                Faker faker = new Faker();
                String name = faker.color().name() + "-" + faker.animal().name() + '-'+ (int)(Math.random()*1000);
                String name_trimmed= name.trim().toLowerCase();

                while(ser.existsByMockName(name_trimmed)){
                    name = faker.color().name() + "-" + faker.animal().name() + '-'+ (int)(Math.random()*1000);
                    name_trimmed= name.trim().toLowerCase();
                }
                while(ser.existsByUuid(uuidGen)){
                    uuidGen = UUID.randomUUID();
                }
                System.out.println("[AUTH] uuid=" + uuidGen);


                SystemEntity se = new SystemEntity(uuidGen, name_trimmed);
                ser.save(se);
                //System.out.println(jsonHash + "\n" + uuidGen);
                return new ResponseEntity<>(uuidGen.toString(), HttpStatusCode.valueOf(200));
            }
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        } catch (Exception e) {
            System.err.println("Teste exception total: "+ test++);
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }
    @GetMapping("/client_auth")
    public ResponseEntity<String> clientAuth(@RequestHeader("User-Agent") String user, @RequestHeader("X-WireSentinel-Credential") String credential, @RequestHeader("X-WireSentinel-UUID") String uuidString, @RequestHeader("X-WireSentinel-Timestamp") String timestamp){
        LocalDateTime date;
        try {
            date = LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        if (date.isBefore(LocalDateTime.now().minusMinutes(3)) || date.isAfter(LocalDateTime.now())) return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        String jsonHash =
                        "{\r\n" +
                        "  X-WireSentinel-Timestamp: "+timestamp+",\r\n" +
                        "  User-Agent: "+user+"\r\n" +
                        "}";
        String algorithm = "HmacSHA256";
        try{
            String hmac = getHmac(jsonHash);
            //System.out.println(jsonHash + "\n" + credential+ "\n" + hmac);
            if (hmac.equals(credential)){
                UUID uuid;
                try{
                    uuid = UUID.fromString(uuidString);
                } catch (IllegalArgumentException e) {
                    return new ResponseEntity<>(HttpStatusCode.valueOf(400));
                }
                if(ser.existsByUuid(uuid)){
                    //System.out.println(jsonHash + "\n" + uuidGen);
                    return new ResponseEntity<>(HttpStatusCode.valueOf(200));
                }
            }
            return new ResponseEntity<>(HttpStatusCode.valueOf(401));
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }
    public String getHmac(String jsonHash){
        String algorithm = "HmacSHA256";
        try{
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    algorithm
            );
            mac.init(keySpec);
            byte[] hashBytes = mac.doFinal(jsonHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
