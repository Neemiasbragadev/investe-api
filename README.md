# investe-api

Agregador de dados públicos brasileiros (cotações de moedas, CEP, feriados) —
projeto de portfólio em Java 25 / Spring Boot 4, focado em mostrar features
modernas da linguagem aplicadas a um problema real: consultar várias APIs
externas em paralelo, com tratamento de erro e timeout, sem quebrar a
aplicação.

> README ainda em construção — a versão completa (arquitetura, decisões
> técnicas, como rodar) é a Fase 7 do roteiro. Por enquanto só a seção de
> paralelismo, que é o entregável da Fase 3.

## Paralelismo: sequencial vs paralelo (virtual threads)

O endpoint `GET /api/agregado?par=USD-BRL&cep=01310930&ano=2026` consulta
3 fontes externas independentes (AwesomeAPI, BrasilAPI CEP, BrasilAPI
feriados). `AgregadorService` tem duas implementações:

- `buscarSequencial` — chama as 3 fontes uma atrás da outra.
- `buscarParalelo` — dispara as 3 chamadas ao mesmo tempo, cada uma numa
  **virtual thread** (`Executors.newVirtualThreadPerTaskExecutor()`), usada
  em produção.

Medido em `AgregadorBenchmarkTest` (WireMock simulando 300ms de latência
igual nas 3 fontes, para o número ser determinístico):

| Estratégia | Tempo |
|---|---|
| Sequencial | ~1196 ms |
| Paralelo (virtual threads) | ~308 ms |

O sequencial fica perto da **soma** das 3 latências (3 × 300ms); o paralelo
fica perto do **maior** tempo individual (300ms) — quase 4x mais rápido com
apenas 3 fontes, e a diferença cresce com mais fontes.

### Por que virtual threads, e não `CompletableFuture` com pool fixo?

Sem virtual threads, para paralelizar 3 chamadas HTTP bloqueantes sem travar
uma thread do SO por chamada, seria preciso `CompletableFuture.supplyAsync`
com um `ExecutorService` de tamanho ajustado à mão (ou migrar para
`WebClient` reativo). Virtual threads deixam escrever o código do jeito
síncrono/óbvio — `future.get()` bloqueando mesmo — e a JVM cuida de não
desperdiçar threads do sistema operacional: cada virtual thread bloqueada
custa quase nada.

Vale notar que isso é diferente do `spring.threads.virtual.enabled=true`
(configurado desde a Fase 1): aquele faz o Tomcat atender cada **requisição
HTTP recebida** numa virtual thread. O executor do `AgregadorService` é
outro uso da mesma feature, para paralelizar as chamadas de **saída** dentro
de uma única requisição.
