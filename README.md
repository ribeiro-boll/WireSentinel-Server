<img width="1857" height="929" alt="dashboard_inicial" src="https://github.com/user-attachments/assets/dfdae3fa-2eb2-4aec-8406-a47650babc07" />

# WireSentinel Server

Servidor central do WireSentinel, desenvolvido em Java com Spring Boot, responsável por receber, autenticar, persistir e expor os dados capturados pelos agentes locais.

O servidor atua como núcleo do sistema. Ele recebe pacotes processados pelo cliente C, valida a autenticidade das requisições, persiste os dados em PostgreSQL e fornece APIs para o frontend consultar sistemas, pacotes e métricas.

---
## Índice

- [Dashboard e Interfaces](#dashboard-e-interfaces)
- [Responsabilidades](#responsabilidades)
- [Arquitetura](#arquitetura)
- [Visão geral do fluxo](#visão-geral-do-fluxo)
- [Fluxos principais](#fluxos-principais)
  - [1. Registro do agente](#1-registro-do-agente)
  - [2. Validação do agente](#2-validação-do-agente)
  - [3. Ingestão de pacotes](#3-ingestão-de-pacotes)
- [Autenticação](#autenticação)
  - [Autenticação do agente](#autenticação-do-agente)
  - [Autenticação do usuário](#autenticação-do-usuário)
- [Endpoints do agente](#endpoints-do-agente)
  - [Registrar agente](#registrar-agente)
  - [Validar agente](#validar-agente)
  - [Enviar pacotes](#enviar-pacotes)
- [Endpoints do usuário](#endpoints-do-usuário)
  - [Cadastro](#cadastro)
  - [Login](#login)
  - [Vincular agente ao usuário](#vincular-agente-ao-usuário)
  - [Remover vínculo com agente](#remover-vínculo-com-agente)
- [Endpoints de métricas](#endpoints-de-métricas)
  - [Listar sistemas do usuário](#listar-sistemas-do-usuário)
  - [Listar pacotes de um sistema](#listar-pacotes-de-um-sistema)
- [Modelos principais](#modelos-principais)
  - [SystemEntity](#systementity)
  - [PacketEntity](#packetentity)
  - [UserEntity](#userentity)
- [Frontend](#frontend)
  - [Fluxo do frontend](#fluxo-do-frontend)
  - [Servindo o frontend pelo Spring](#servindo-o-frontend-pelo-spring)
  - [Rotas SPA](#rotas-spa)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Docker Compose](#docker-compose)
- [Dockerfile](#dockerfile)
- [Build e execução do servidor](#build-e-execução-do-servidor)
  - [Executar com Docker Compose](#executar-com-docker-compose)
- [Build e deploy do frontend no Spring](#build-e-deploy-do-frontend-no-spring)
- [Segurança](#segurança)
  - [Agente](#agente)
  - [Usuário](#usuário)
- [Paginação e ordenação](#paginação-e-ordenação)
- [Checklist operacional](#checklist-operacional)
- [Resumo](#resumo)

---

## Dashboard e Interfaces

### Dashboard de Pacotes

<p align="center">
  <img src="https://github.com/user-attachments/assets/e825cff2-079b-4ed9-9304-c839a8ee0447" width="66%" alt="dashboard_pacotes_1" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/d6361e88-79b2-4a38-9afd-2cffd7014457" width="66%" alt="dashboard_pacotes_2" />
</p>

---

### Visualização Completa de Pacote

<p align="center">
  <img src="https://github.com/user-attachments/assets/1b1e7067-0f4a-4d7a-82e5-b802b8552aac" width="66%" alt="pacote_completo" />
</p>

---

### Interface de Sistemas

<p align="center">
  <img src="https://github.com/user-attachments/assets/41f21e2d-91cd-4a1c-915d-ddba56fe9ad5" width="66%" alt="interface_sistemas" />
</p>

---

## Responsabilidades

1. Registrar agentes WireSentinel.
2. Validar agentes usando UUID e HMAC SHA-256.
3. Receber pacotes processados via HTTP.
4. Persistir sistemas e pacotes no PostgreSQL.
5. Gerenciar usuários com autenticação JWT.
6. Vincular agentes a usuários.
7. Expor APIs paginadas para consulta de sistemas e pacotes.
8. Servir o frontend estático React.
9. Permitir visualização dos dados capturados pelo dashboard web.

---

## Arquitetura

O servidor é organizado em três grandes responsabilidades:

- **API do agente:** endpoints usados pelo cliente C para registro, autenticação e ingestão.
- **API do usuário:** endpoints usados pelo frontend para login, cadastro e vínculo de agentes.
- **API de métricas:** endpoints usados pelo frontend para consultar sistemas e pacotes.

O frontend é buildado como aplicação estática e servido pelo próprio Spring Boot a partir de:

```text
src/main/resources/static/
````

---

## Visão geral do fluxo

```text
WireSentinel Client
        │
        ▼
HTTP + HMAC SHA-256
        │
        ▼
WireSentinel Server
        │
        ├── Validação de timestamp
        ├── Validação de HMAC
        ├── Validação de UUID
        │
        ▼
PostgreSQL
        │
        ▼
API protegida por JWT
        │
        ▼
Frontend React / Dashboard
```

---

# Fluxos principais

## 1. Registro do agente

Na primeira execução, o cliente C registra a máquina monitorada no servidor.

Fluxo:

1. O cliente gera um timestamp local.
2. O cliente monta uma string de assinatura.
3. O cliente gera um HMAC SHA-256 usando a chave compartilhada.
4. O cliente envia uma requisição para:

```http
GET /api/register
```

5. O servidor valida:

   * timestamp;
   * HMAC;
   * User-Agent.

6. Se a requisição for válida, o servidor cria um novo sistema.

7. O servidor gera um UUID.

8. O servidor gera um nome identificável para o sistema.

9. O servidor salva o sistema no banco.

10. O servidor retorna o UUID em texto puro.

11. O cliente salva esse UUID localmente no arquivo `.uuid`.

Resposta de sucesso:

```text
550e8400-e29b-41d4-a716-446655440000
```

---

## 2. Validação do agente

Quando o cliente já possui um UUID salvo, ele pode validar esse UUID com o servidor.

Endpoint:

```http
GET /api/client_auth
```

Fluxo:

1. O cliente lê o UUID local.
2. O cliente gera timestamp e HMAC.
3. O cliente envia o UUID no header.
4. O servidor valida:

   * timestamp;
   * HMAC;
   * formato do UUID;
   * existência do UUID no banco.

Se tudo estiver correto, o servidor responde `200 OK`.

---

## 3. Ingestão de pacotes

Depois de capturar e processar pacotes, o cliente C envia os dados em lote para o servidor.

Endpoint:

```http
POST /api/ingest
```

Fluxo:

1. O cliente captura pacotes via raw socket.
2. O cliente processa Ethernet, VLAN, IPv4, IPv6, TCP, UDP, ICMP e outros protocolos.
3. O cliente monta estruturas internas de pacote.
4. O cliente serializa os pacotes em JSON.
5. O cliente calcula o `Content-Length`.
6. O cliente gera timestamp e HMAC.
7. O cliente envia o batch para o servidor.
8. O servidor valida a requisição.
9. O servidor associa os pacotes ao UUID enviado no header.
10. O servidor persiste os pacotes no PostgreSQL.

Formato do body:

```json
{
  "packets": [
    {
      "timestamp": "2026-05-07T12:09:06",
      "macAdressOrigem": "aa:bb:cc:dd:ee:ff",
      "macAdressDestino": "11:22:33:44:55:66",
      "protocoloIp": "IPv4",
      "ipOrigem": "192.168.0.10",
      "ipDestino": "8.8.8.8",
      "tempoDeVida": 64,
      "tamanhoTotalHeader": 54,
      "tamanhoTotalPacote": 1500,
      "portaOrigem": 51544,
      "portaDestino": 443,
      "tcpSeq": 123456789,
      "tcpAckSeq": 987654321,
      "tcpAck": true,
      "tcpFin": false,
      "tcpSyn": true,
      "tcpRst": false,
      "tcpPsh": false,
      "tcpUrg": false,
      "tcpCwr": false,
      "tcpEce": false,
      "isVlan": false,
      "protocolo_transporte": "TCP",
      "protocolo_aplicacao": "https"
    }
  ]
}
```

---

# Autenticação

O servidor utiliza dois modelos de autenticação, cada um para um tipo de consumidor.

---

## Autenticação do agente

O agente C não usa JWT.

A comunicação entre agente e servidor usa HMAC SHA-256 com uma chave compartilhada.

Variável usada no servidor:

```env
WIRESENTINEL_SECRET=chave_compartilhada
```

O cliente deve usar a mesma chave no arquivo `.security`:

```text
SHRD_SCRT=chave_compartilhada
URL=192.168.0.10
PORT=8080
```

Headers usados pelo agente:

```http
User-Agent: WireSentinel-Agent/1.0
X-WireSentinel-Timestamp: <timestamp>
X-WireSentinel-Credential: <hmac_sha256>
X-WireSentinel-UUID: <uuid_do_agente>
```

O header `X-WireSentinel-UUID` é usado em `/api/client_auth` e `/api/ingest`.

---

## Autenticação do usuário

Usuários do dashboard utilizam JWT.

Variável usada no servidor:

```env
JWT_SECRET=chave_jwt
```

Fluxo:

1. O usuário cria uma conta ou faz login.
2. O servidor retorna um JWT em texto puro.
3. O frontend salva o token.
4. O frontend envia o token em rotas protegidas:

```http
Authorization: Bearer <token>
```

---

# Endpoints do agente

## Registrar agente

```http
GET /api/register
```

Usado pelo cliente C para criar um novo sistema no servidor.

Headers:

```http
User-Agent: WireSentinel-Agent/1.0
X-WireSentinel-Timestamp: <timestamp>
X-WireSentinel-Credential: <hmac_sha256>
```

Resposta:

```text
<uuid_gerado>
```

---

## Validar agente

```http
GET /api/client_auth
```

Usado pelo cliente C para validar se o UUID salvo localmente ainda existe no servidor.

Headers:

```http
User-Agent: WireSentinel-Agent/1.0
X-WireSentinel-Timestamp: <timestamp>
X-WireSentinel-Credential: <hmac_sha256>
X-WireSentinel-UUID: <uuid_do_agente>
```

Resposta de sucesso:

```http
200 OK
```

---

## Enviar pacotes

```http
POST /api/ingest
```

Usado pelo cliente C para enviar pacotes processados em lote.

Headers:

```http
Content-Type: application/json
Content-Length: <tamanho_do_body>
User-Agent: WireSentinel-Agent/1.0
X-WireSentinel-Timestamp: <timestamp>
X-WireSentinel-Credential: <hmac_sha256>
X-WireSentinel-UUID: <uuid_do_agente>
```

Body:

```json
{
  "packets": []
}
```

Resposta de sucesso:

```http
200 OK
```

---

# Endpoints do usuário

## Cadastro

```http
POST /api/user/register
```

Body:

```json
{
  "login": "usuario",
  "password": "senha"
}
```

Resposta de sucesso:

```text
<jwt>
```

---

## Login

```http
POST /api/user/login
```

Body:

```json
{
  "login": "usuario",
  "password": "senha"
}
```

Resposta de sucesso:

```text
<jwt>
```

---

## Vincular agente ao usuário

```http
POST /api/user/register_uuid/{uuid}
```

Autenticação:

```http
Authorization: Bearer <token>
```

Uso:

```text
POST /api/user/register_uuid/550e8400-e29b-41d4-a716-446655440000
```

Esse endpoint vincula um agente já registrado no servidor à conta do usuário autenticado.

---

## Remover vínculo com agente

```http
POST /api/user/remove_uuid/{uuid}
```

Autenticação:

```http
Authorization: Bearer <token>
```

Esse endpoint remove o vínculo entre o usuário autenticado e o agente informado.

---

# Endpoints de métricas

## Listar sistemas do usuário

```http
GET /api/metrics/systems
```

Autenticação:

```http
Authorization: Bearer <token>
```

Suporta paginação:

```http
GET /api/metrics/systems?page=0&size=10
```

Resposta:

```json
{
  "content": [
    {
      "id": 1,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "mockName": "purple-wolf-123"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

---

## Listar pacotes de um sistema

```http
GET /api/metrics/system/{uuid}/packets
```

Autenticação:

```http
Authorization: Bearer <token>
```

Suporta paginação e ordenação:

```http
GET /api/metrics/system/{uuid}/packets?page=0&size=50&sort=id,desc
```

Exemplo:

```http
GET /api/metrics/system/550e8400-e29b-41d4-a716-446655440000/packets?page=0&size=50&sort=id,desc
```

Resposta:

```json
{
  "content": [
    {
      "id": 123,
      "timestamp": "2026-05-07T12:09:06",
      "macAdressOrigem": "aa:bb:cc:dd:ee:ff",
      "macAdressDestino": "11:22:33:44:55:66",
      "protocoloIp": "IPv4",
      "ipOrigem": "192.168.0.10",
      "ipDestino": "8.8.8.8",
      "tempoDeVida": 64,
      "tamanhoTotalHeader": 54,
      "tamanhoTotalPacote": 1500,
      "portaOrigem": 51544,
      "portaDestino": 443,
      "tcpSeq": 123456789,
      "tcpAckSeq": 987654321,
      "tcpAck": true,
      "tcpFin": false,
      "tcpSyn": true,
      "tcpRst": false,
      "tcpPsh": false,
      "tcpUrg": false,
      "tcpCwr": false,
      "tcpEce": false,
      "isVlan": false,
      "protocolo_transporte": "TCP",
      "protocolo_aplicacao": "https",
      "uuid": "550e8400-e29b-41d4-a716-446655440000"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 50,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

---

# Modelos principais

## SystemEntity

Representa um agente/sistema registrado.

Campos principais:

```text
id
uuid
mockName
```

Uso:

* identificar máquinas monitoradas;
* vincular agentes a usuários;
* consultar pacotes de um sistema específico.

---

## PacketEntity

Representa um pacote processado pelo agente.

Campos principais:

```text
id
timestamp
macAdressOrigem
macAdressDestino
protocoloIp
ipOrigem
ipDestino
tempoDeVida
tamanhoTotalHeader
tamanhoTotalPacote
portaOrigem
portaDestino
tcpSeq
tcpAckSeq
tcpAck
tcpFin
tcpSyn
tcpRst
tcpPsh
tcpUrg
tcpCwr
tcpEce
isVlan
protocolo_transporte
protocolo_aplicacao
uuid
```

---

## UserEntity

Representa um usuário do dashboard.

Responsabilidades:

* autenticação via login e senha;
* vínculo com UUIDs de agentes;
* acesso aos sistemas associados.

---

# Frontend

O frontend do WireSentinel é uma aplicação React estática servida pelo próprio Spring Boot.

Ele consome apenas os endpoints HTTP do servidor.

O frontend permite:

1. Criar usuário.
2. Fazer login.
3. Vincular UUID de agente.
4. Listar sistemas vinculados.
5. Consultar pacotes de cada sistema.
6. Visualizar métricas e gráficos.
7. Filtrar pacotes.
8. Atualizar dados automaticamente.

---

## Fluxo do frontend

```text
Usuário acessa o dashboard
        │
        ▼
Login ou cadastro
        │
        ▼
JWT salvo no navegador
        │
        ▼
Usuário vincula UUID do agente
        │
        ▼
Frontend consulta /api/metrics/systems
        │
        ▼
Usuário abre um sistema
        │
        ▼
Frontend consulta /api/metrics/system/{uuid}/packets
        │
        ▼
Dashboard exibe tabela, filtros e gráficos
```

---

## Servindo o frontend pelo Spring

Após o build do frontend, os arquivos devem ser copiados para:

```text
src/main/resources/static/
```

Estrutura esperada:

```text
src/main/resources/static/index.html
src/main/resources/static/assets/
```

O Spring Boot detecta automaticamente o `index.html` e serve a aplicação.

---

## Rotas SPA

Como o frontend usa rotas client-side, o servidor precisa encaminhar algumas rotas para o `index.html`.

Exemplo de controller:

```java
package com.bolota.wiresentinelserver.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
            "/login",
            "/register",
            "/dashboard",
            "/systems",
            "/systems/{path:[^\\.]*}",
            "/link",
            "/docs"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
```

As rotas `/api/**` não devem ser encaminhadas para o frontend.

---

# Variáveis de ambiente

O servidor utiliza variáveis de ambiente para configurar autenticação e banco de dados.

Exemplo `.env`:

```env
JWT_SECRET=chave_jwt_grande_e_segura
WIRESENTINEL_SECRET=chave_compartilhada_do_agente
```

Variáveis usadas pelo container da aplicação:

```env
JWT_SECRET
WIRESENTINEL_SECRET
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
TZ
```

---

# Build e execução do servidor

## Executar com Docker Compose

```bash
docker compose up -d --build
```

Ver logs da aplicação:

```bash
docker compose logs -f app
```

Ver logs do banco:

```bash
docker compose logs -f db
```

Parar os containers:

```bash
docker compose down
```

---

Buildar e subir o servidor:

```bash
docker compose up -d --build
```

Após isso, o dashboard estará disponível em:

```text
http://localhost:8080
```

Ou pelo IP da máquina:

```text
http://<ip-do-servidor>:8080
```

---

# Segurança

O WireSentinel separa autenticação de agente e autenticação de usuário.

## Agente

Usa:

```text
HMAC SHA-256
Timestamp
UUID
Chave compartilhada
```

Objetivo:

* validar que o agente possui a chave correta;
* evitar ingestão de dados por clientes não autorizados;
* associar pacotes ao sistema correto.

## Usuário

Usa:

```text
JWT
Authorization Bearer
```

Objetivo:

* proteger as rotas do dashboard;
* permitir que cada usuário acesse apenas seus sistemas vinculados;
* separar autenticação humana da autenticação de máquina.

---

# Paginação e ordenação

As consultas de sistemas e pacotes usam paginação do Spring.

Exemplos:

```http
GET /api/metrics/systems?page=0&size=10
```

```http
GET /api/metrics/system/{uuid}/packets?page=0&size=50&sort=id,desc
```

Ordenações úteis:

```text
id,desc
id,asc
timestamp,desc
timestamp,asc
tamanhoTotalPacote,desc
tamanhoTotalPacote,asc
portaDestino,asc
portaDestino,desc
```

---

# Checklist operacional

Antes de executar o ambiente:

1. Criar o arquivo `.env`.
2. Definir `JWT_SECRET`.
3. Definir `WIRESENTINEL_SECRET`.
4. Confirmar que o `compose.yaml` expõe a porta `8080`.
5. Buildar o frontend.
6. Copiar o frontend para `src/main/resources/static`.
7. Buildar a imagem do servidor.
8. Subir o ambiente com Docker Compose.
9. Validar se o Spring iniciou na porta `8080`.
10. Validar se o PostgreSQL está saudável.
11. Registrar um agente.
12. Vincular o UUID do agente no dashboard.
13. Consultar pacotes pelo frontend.

---

# Resumo

O WireSentinel Server centraliza todo o fluxo da aplicação:

```text
Agente C
   ↓
Registro por UUID
   ↓
Autenticação HMAC
   ↓
Ingestão de pacotes
   ↓
Persistência PostgreSQL
   ↓
API protegida por JWT
   ↓
Frontend React
   ↓
Dashboard de observabilidade
```

O servidor fornece a base de autenticação, persistência, consulta e visualização dos dados capturados pelos agentes WireSentinel.
