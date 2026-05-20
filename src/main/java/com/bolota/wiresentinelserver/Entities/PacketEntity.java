package com.bolota.wiresentinelserver.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PacketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String macAdressOrigem;
    private String macAdressDestino;
    private String protocoloIp;
    private String ipOrigem;
    private String ipDestino;
    private Integer tempoDeVida;
    private Integer tamanhoTotalHeader;
    private Integer tamanhoTotalPacote;
    private Integer portaOrigem;
    private Integer portaDestino;
    private Long tcpSeq;
    private Long tcpAckSeq;
    private Boolean tcpAck;
    private Boolean tcpFin;
    private Boolean tcpSyn;
    private Boolean tcpRst;
    private Boolean tcpPsh;
    private Boolean tcpUrg;
    private Boolean tcpCwr;
    private Boolean tcpEce;
    private Boolean isVlan;
    private String protocolo_transporte;
    private String protocolo_aplicacao;
    private UUID uuid;
}
