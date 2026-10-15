# LUTI CHAOS

Mod Java para Hytale 0.7.1, baseado em Random Chaos 1.1.0, de RCBaito.

**Estado:** compilado e carregado pelo servidor real 0.7.1. Dezessete testes automatizados passaram. HUD, combate, transferencias e `/chaos start` com jogador ainda precisam da validacao dentro do Hytale descrita em `docs/VALIDACAO.md`.

## Instalar e jogar

1. Feche o mundo e faca uma copia de seguranca dele.
2. Remova/desative o Random Chaos original: ambos registram `/chaos` e nao devem ser usados juntos.
3. Copie `dist/LutiChaos-1.0.0.jar` para `%APPDATA%\Hytale\UserData\Mods`.
4. No Hytale, abra as configuracoes do mundo existente, habilite **LutiPlay:LutiChaos** na lista de mods e carregue o mundo. Se sua versao do gerenciador permitir instalar um arquivo local, selecione esse mesmo JAR. Nao e necessario criar outro mundo nem usar servidor externo.
5. Com permissoes de operador do seu mundo, execute `/chaos start`. Seu jogador entra automaticamente como participante. O primeiro evento acontece apos 30 segundos reais.
6. Use `/chaos stop` para terminar e restaurar os efeitos temporarios.

Se o comando for negado, habilite as permissoes de operador nas configuracoes do mundo. O comando exige `luti.chaos.manage`; para administracao avancada, conceda esse node ao proprietario pelo sistema de permissoes do Hytale. Nao e concedido acesso administrativo irrestrito a qualquer jogador.

Para servidor dedicado, use a pasta `mods/` ao lado dos arquivos de dados do servidor. O servidor precisa ser exatamente 0.7.1; o manifesto declara `=0.7.1`. A compilacao nao foi validada contra pre-release.

A entrega fica em `dist/`; nenhum arquivo foi instalado automaticamente no jogo e nenhum save existente foi alterado.

## Comandos

| Comando | Funcao |
|---|---|
| `/chaos start` | Inicia; adiciona quem executou; chamadas repetidas nao duplicam o timer |
| `/chaos stop` | Cancela o timer, efeitos temporarios, NPCs rastreados e HUD |
| `/chaos interval 30` | Intervalo de 5 a 3600 segundos; reinicia a contagem sem zerar a lista de desafios ja sorteados; vale nesta sessao |
| `/chaos add Nome` | Adiciona jogador conectado no mesmo mundo |
| `/chaos remove Nome` | Remove e limpa efeitos do participante |
| `/chaos events` | Lista IDs e estado, inclusive falhas/desativados |
| `/chaos status` | Estado, contagem e ultimo evento |
| `/chaos help` | Ajuda |

Um desafio por instancia do plugin; participantes precisam estar no mesmo mundo. Adicionados durante um evento entram no proximo sorteio. Se todos sairem, o desafio termina. Cada evento ativo aparece uma unica vez por sessao. A HUD mostra Desafio N/total, calculado pelos eventos realmente habilitados. Depois do ultimo, o efeito termina e o desafio encerra; use /chaos start para embaralhar uma nova sequencia. O evento do coco foi removido.

## Configuracao

O plugin cria `luti-chaos.properties` em seu diretorio de dados (`mods/LutiPlay_LutiChaos/` no teste dedicado). Em single-player, procure essa pasta de dados nos arquivos do mundo/servidor interno; o local e definido pelo Hytale. Edite com o mundo fechado e reabra.

```properties
intervalSeconds=30
effectDurationSeconds=30
maxEntitiesPerEvent=20
hud=true
allowInstantDeath=false
event.kill=true
event.mlg=true
```

Cada evento possui `event.<id>=true/false`. Instant Death exige **ambos** `allowInstantDeath=true` e `event.kill=true`. Intervalo e limitado a 5–3600; efeitos a 1–600 segundos; NPCs a 0–100. O limite de NPCs e compartilhado por todos os participantes daquele evento. Os efeitos anteriores terminam ao sortear o proximo evento; portanto sua duracao efetiva e limitada pelo intervalo.

## Eventos — somente para atrapalhar

O sorteio usa 13 desafios, dos quais 12 ficam ativos por padrao. Os eventos antigos de rifle, Criativo, vida extra, armadura gratuita, Sonic, Super Pulo e barras visuais foram retirados do registro; opcoes antigas no arquivo nao os reativam.

| Evento | ID | Comportamento |
|---|---|---|
| Visitante Indesejado | randommob | Um mob hostil aleatorio aparece a 2,5–4 blocos do jogador |
| Emboscada | ambush | Tres mobs hostis aleatorios |
| Horda Aleatoria | horde | Cinco mobs hostis aleatorios |
| Chuva de Aranhas | spiderrain | Aranhas hostis caem de pouca altura perto do jogador |
| Esquadrao de Sapos | toadsquad | Ate quatro sapos de magma por participante |
| Vida de Vidro | glasscanon | Recebe 3x mais dano; vida maxima normal |
| Pacifista | pacifist | Dano causado zerado temporariamente |
| Bracos Fracos | weakarms | Apenas 25% do dano normal |
| Modo Caracol | snail | Velocidade reduzida |
| Sapatos de Cimento | cement | Pulo desativado |
| Peso Morto | heavy | Movimento lento e pulo baixo |
| Desafio MLG | mlg | Queda de 300 blocos, balde e controle horizontal no ar |
| Morte Instantanea | kill | Bloqueado por padrao; exige ativacao explicita |

Mobs sorteados: Aranha, Lobo Negro, Esqueleto Guerreiro, Trork Guerreiro e Sapo de Magma. O tipo sorteado aparece no chat. Somente roles existentes na API em execucao participam. A agressividade depende da IA nativa; comportamento e posicionamento precisam de teste em jogo. O limite configurado de entidades continua compartilhado entre participantes. NPCs rastreados sao removidos ao terminar seu evento.

MLG: os parametros airSpeedMultiplier e airControlMin/MaxSpeed/Multiplier sao ajustados para permitir comecar a mover no ar, inclusive apos um teleporte sem velocidade horizontal. Usa WASD; sem voo, invulnerabilidade ou alteracao de gravidade. Os cinco valores anteriores sao restaurados ao aterrissar, entrar na agua, morrer ou ao detectar um novo pulo apos a queda; ha timeout de 20s. O ajuste nao e reaplicado durante a rodada. Stop e remocao do jogador tambem restauram. O balde e mantido como ferramenta para tentar sobreviver. A intensidade do controle ainda precisa de validacao durante a queda no cliente.

## HUD

Painel de 300 x 184 no canto superior direito: LUTI CHAOS, segundos restantes, barra, nome em portugues, duas linhas de descricao e aviso nos cinco segundos finais. Nome/descricao tambem aparecem no chat. Usa CustomUIHud com chave luti-chaos e atualizacoes incrementais; a barra e enviada como objeto Anchor completo.

## Compilar

Requer **JDK 25** e acesso a internet para o Gradle Wrapper 9.2.0 e JUnit. O JRE do jogo nao substitui o JDK.

```powershell
$env:JAVA_HOME = 'C:\caminho\para\jdk-25'
.\gradlew.bat clean dist
```

Por padrao usa o HytaleServer.jar da instalacao release do Windows, sem inclui-lo no mod. Para outro local:

```powershell
.\gradlew.bat dist '-PhytaleServerJar=C:/Hytale/Server/HytaleServer.jar'
```

Resultado: `dist/LutiChaos-1.0.0.jar`. O diretorio `libs/` foi herdado do original; seu JAR antigo nao e usado. `.tools/` contem ferramentas locais e dados isolados dos testes; nao integra a distribuicao.

## Creditos, compatibilidade e testes

Original: https://github.com/rcbaito/randomchaos — commit `78fb418116eb8934b44215b73975fedde3cc42ee`.

O README original declara MIT, mas o commit clonado nao contem arquivo LICENSE nem texto integral da licenca. Os creditos foram preservados no manifesto e em NOTICE; nao foi inventado um copyright ou uma confirmacao de licenca ausente. Consulte `NOTICE.md` antes de redistribuir publicamente.

Detalhes: `docs/VALIDACAO.md` e `docs/COMPATIBILIDADE.md`.

Referencias oficiais consultadas: https://docs.hytale.com/ (indice consultado exibe release 0.7.0). A API efetiva utilizada foi inspecionada diretamente no HytaleServer.jar **0.7.1** instalado; essa evidencia prevalece para esta compilacao.




