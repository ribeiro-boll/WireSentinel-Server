package com.bolota.wiresentinelserver.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class GeneralService {
    @Value("${wiresentinel.sharedkey}")
    private String secret;

}

