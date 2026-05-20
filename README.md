from pathlib import Path

content = """# Checklist de Correções — WireSentinel Server

## 1. Bloqueadores

- [x] Corrigir cadastro de usuário
    - [x] Garantir que o login seja salvo em `login`
    - [x] Garantir que a senha criptografada seja salva em `passwordHash`
    - [x] Remover qualquer `setLogin(...)` duplicado usado para senha

- [x] Corrigir rotas liberadas no `SecurityConfig`
    - [x] Liberar `/api/user/register`
    - [x] Liberar `/api/user/login`
    - [x] Conferir se as rotas configuradas batem exatamente com os controllers

- [x] Setar o UUID nos pacotes antes de salvar
    - [x] Pegar UUID do header
    - [x] Validar se o UUID existe no banco
    - [x] Aplicar esse UUID em cada `PacketEntity`
    - [x] Só depois chamar `saveAll(...)`

- [x] Corrigir autorização no `MetricsService`
    - [x] Se o usuário **não** contém o UUID, retornar `401` ou `403`
    - [x] Se o usuário contém o UUID, retornar os pacotes
    - [x] Garantir que usuário não consiga consultar sistema que não vinculou

- [x] Validar UUID antes de vincular ao usuário
    - [x] Antes de adicionar UUID na lista do usuário, checar `ser.existsByUuid(uuid)`
    - [x] Se não existir, retornar `404` ou `400`

---

## 2. Persistência / JPA

- [ ] Mapear corretamente a lista de UUIDs do usuário
    - [x] Se `UserEntity` tem `List<UUID> uuids`, usar `@ElementCollection`
    - [ ] Conferir se o Hibernate cria tabela auxiliar para essa lista

- [x] Adicionar unicidade no banco
    - [x] `SystemEntity.uuid` deve ser único
    - [x] `SystemEntity.mockName` ou `name` deve ser único
    - [x] Login do usuário também deve ser único
---

## 3. Segurança do agente

- [x] Validar janela de tempo do timestamp
    - [ ] Não comparar apenas “mesmo minuto”
    - [ ] Usar diferença absoluta em segundos
    - [ ] Aceitar algo como `<= 60s` ou `<= 120s`

- [x] Aplicar validação de timestamp em todos os endpoints sensíveis
    - [ ] `/api/register`
    - [ ] `/api/client_auth`
    - [ ] `/api/ingest`

- [x] Receber timestamp como `String` nos endpoints com HMAC
    - [ ] Evitar que `LocalDateTime` altere a representação textual usada no HMAC
    - [ ] Converter para `LocalDateTime` só depois da validação textual, se necessário

- [x] Remover aspas da credential no agente
    - [ ] Usar `X-WireSentinel-Credential: abc123`
    - [ ] Não usar `X-WireSentinel-Credential: "abc123"`

- [ ] Melhorar comparação de HMAC futuramente
    - [ ] Trocar `equals(...)` por comparação em tempo constante
    - [ ] Exemplo conceitual: `MessageDigest.isEqual(...)`

- [ ] Avaliar assinar também o body
    - [ ] MVP atual: timestamp + length
    - [ ] Melhor futuro: timestamp + length + hash do body

---

## 4. API / Design

- [x] Trocar GET com body
    - [ ] Evitar `GET` com `@RequestBody`
    - [ ] Preferir `/api/metrics/system/{uuid}/packets`
    - [ ] Ou usar query param: `/api/metrics/system/packets?uuid=...`

- [x] Retornar UUID e nome no registro do agente
    - [ ] Hoje o backend gera nome aleatório
    - [ ] Responder algo como:
      ```json
      {
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "name": "blue-wolf-123"
      }
      ```

- [ ] Normalizar nome gerado pelo Faker
    - [ ] Tudo minúsculo
    - [ ] Sem espaços
    - [ ] Sem caracteres estranhos
    - [ ] Exemplo: `blue-wolf-123`

- [ ] Usar status HTTP mais expressivos
    - [ ] `201 Created` para registro criado
    - [ ] `202 Accepted` para ingestão aceita
    - [ ] `400 Bad Request` para header/body inválido
    - [ ] `401 Unauthorized` para HMAC/JWT inválido
    - [ ] `403 Forbidden` para usuário sem acesso ao UUID
    - [ ] `404 Not Found` para UUID inexistente
    - [ ] `409 Conflict` para login/nome duplicado

---

## 5. Limpeza / TTL

- [ ] Confirmar campo usado no cleanup
    - [ ] Se usar `timestamp`, lembrar que ele vem do agente
    - [ ] Se o relógio do agente estiver errado, o TTL pode ficar errado

- [ ] Considerar adicionar `receivedAt`
    - [ ] `capturedAt`: horário vindo do agente
    - [ ] `receivedAt`: horário gerado pelo backend
    - [ ] TTL deveria usar `receivedAt`

- [ ] Melhorar log do cleanup
    - [ ] Logar quantos pacotes foram removidos
    - [ ] Logar o limite usado, exemplo: `now - 3 days`

---

## 6. Docker / Projeto

- [ ] Garantir que arquivos sensíveis não vão para o GitHub
    - [ ] `.env`
    - [ ] `.idea`
    - [ ] `target`
    - [ ] `.git` dentro de zip/release

- [ ] Conferir `.gitignore`
    - [ ] Ignorar `.env`
    - [ ] Ignorar `target/`
    - [ ] Ignorar arquivos da IDE

- [ ] Otimizar Dockerfile
    - [ ] Copiar `pom.xml` antes de `src`
    - [ ] Baixar dependências antes
    - [ ] Reaproveitar cache do Maven
    - [ ] Evitar `--no-cache` no dia a dia

- [ ] Rebuildar apenas o app quando mudar código Java
    - [ ] `docker compose up -d --build app`
    - [ ] Não usar `docker compose restart` esperando pegar código novo

---

## 7. Organização de código

- [ ] Mover lógica de HMAC para um service
    - [ ] Controller deve só coordenar request/response
    - [ ] HMAC deveria ficar em algo como `HmacService`

- [ ] Mover lógica de registro de sistema para service
    - [ ] Controller chama service
    - [ ] Service gera UUID, nome e salva

- [ ] Remover imports inúteis
    - [ ] Limpar imports duplicados
    - [ ] Remover código comentado antigo

- [ ] Corrigir typo de pacote
    - [ ] `Sercurity` → `Security`

- [ ] Padronizar nomes Java
    - [ ] Usar `camelCase` em Java
    - [ ] Exemplo: `protocoloTransporte`
    - [ ] Evitar campos Java em `snake_case`
    - [ ] Resolver diferença com JSON usando Jackson

---

## 8. Ordem recomendada de correção

1. Corrigir `passwordHash` no cadastro de usuário
2. Corrigir rotas liberadas no `SecurityConfig`
3. Corrigir autorização invertida no `MetricsService`
4. Setar UUID nos pacotes antes de salvar
5. Validar UUID existente antes de vincular usuário
6. Mapear `List<UUID>` com `@ElementCollection`
7. Validar timestamp recente no HMAC
8. Retornar `uuid + name` no registro do agente
9. Trocar GET com body por path variable ou query param
10. Limpar Docker/GitHub/estrutura final

---

## Estado esperado depois disso

- [ ] Agente registra sistema
- [ ] Backend gera UUID e nome amigável
- [ ] Backend salva sistema no banco
- [ ] Agente envia batch com UUID e HMAC
- [ ] Backend valida UUID, HMAC e timestamp
- [ ] Backend salva pacotes com UUID correto
- [ ] Usuário cria conta e faz login
- [ ] Usuário vincula UUID existente à própria conta
- [ ] Usuário só consulta métricas dos sistemas vinculados
- [ ] Cleanup remove pacotes antigos automaticamente

Depois dessa checklist, o backend pode ser considerado um **MVP finalizado e apresentável para portfólio/estágio**.
"""

path = Path("/mnt/data/checklist-wiresentinel-server.md")
path.write_text(content, encoding="utf-8")
path.as_posix()
