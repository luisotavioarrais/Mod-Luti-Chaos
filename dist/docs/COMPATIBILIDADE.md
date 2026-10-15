# Compatibilidade e investigacao

## Ambiente alvo

Hytale release instalado: 0.7.1, revisao 69c03ffa58d7e2f46bce8b0356ed206cfc7f10e6, Java 25. SHA256 HytaleServer.jar: A60FA0B31F1418691F8D8F7CC1159693E3EC504D4F0567FB91493A842D71F8CA.

API verificada com javap do JDK 25 contra esse arquivo. Nao se usaram stubs inventados ou bibliotecas comunitarias como substituto da API.

## Problemas encontrados e correcoes

- Upstream usa servidor de janeiro de 2026. A API 0.7.1 removeu Vector3d/Vector3f antigos: migrados para JOML; rotacao de NPC para Rotation3f.
- CustomUIHud agora exige chave; setCustomHud foi substituido por addCustomHud/removeCustomHud. Usada chave exclusiva e update incremental.
- Inventory/getCombinedHotbarFirst obsoletos: migrados para InventoryComponent.getCombined e HOTBAR_FIRST.
- Manifesto exige intervalo semantico: bare 0.7.1 foi rejeitado no teste real; corrigido para =0.7.1.
- Original conta 60 ticks e so sorteia no tick seguinte: deriva temporal e erro de um segundo. Agora deadline monotonic em nanos, sem descontar baseado na velocidade da barra.
- Uma referencia global de store era usada com jogadores de mundos diferentes; agora existe validacao de pertencimento e recusa de participantes de outro mundo.
- Timer original podia deixar callbacks enfileirados apos stop; agora token de geracao invalida trabalho antigo e uma flag limita a uma tarefa pendente.
- Original engolia diversas falhas e podia parar tudo no erro central. Falha no start/tick/stop desativa o evento para a sessao, registra stack trace e preserva o timer. Falhas estruturais no loop ainda encerram com limpeza e log.
- Dano buscava metodos por reflexao: agora Damage.EntitySource.getRef verificado na API.
- Velocidade/pulo restauravam numeros aproximados: agora capturam valores anteriores. Criativo restaura modo anterior. Modificadores de saude removidos por chave.
- Random Armor apagava equipamentos: agora entrega pecas sem limpar slots e filtra assets existentes.
- NPCs nao tinham limite nem limpeza: agora orcamento por evento compartilhado e referencias rastreadas.
- Sistemas de remocao de jogador restauram dados antes de salvar/transferir e removem componentes temporarios do holder.

## Adaptacoes deliberadas

2X e 5X Speed Bar passaram a indicadores visuais intermitentes, pois acelerar o sorteio violaria o requisito de segundos reais. Todos os efeitos usam a duracao configurada (30s por padrao) e nao se sobrepoem: o proximo sorteio encerra o anterior. Random Armor entrega em vez de sobrescrever equipamentos.

17 eventos registrados; Instant Death bloqueado por padrao. Falhas de assets em runtime desabilitam apenas o evento que falhou. A compatibilidade de gameplay de cada um ainda precisa de ensaio com cliente. Rifle conserva as flechas do upstream; nao foi confirmado que sejam a municao correta na 0.7.1.

## Limites conhecidos

Um desafio global por instancia; multiplos mundos simultaneos nao sao suportados. Pausas sem ticks de mundo atrasam a execucao efetiva; o relogio usa tempo real e nao tenta executar rajadas para compensar. Nao ha garantia de limpeza apos encerramento forcado do processo; o fluxo normal de saida e o alvo dos hooks implementados. A HUD depende do parser/renderizador do cliente, nao exercitado pelo teste de boot.

A causa historica exata da queda reportada nao pode ser atribuida sem stack trace daquele servidor. As incompatibilidades acima foram observadas no codigo e/ou reproduzidas na compilacao e boot; nao se afirma que todas tenham causado aquela queda especifica.

Licenca: MIT aparece apenas como declaracao no README upstream; ausencia do texto integral registrada em NOTICE.md.

## Revisao atual: somente desafios

Esta revisao substitui a lista original de 17 eventos descrita acima. Agora sao 14 registrados, com Instant Death desativado por padrao. Rifle, Creative, Tank, Sonic, Super Jump, Random Armor e os dois Speed Bar nao sao registrados. ChickenRainEvent foi reaproveitado como spiderrain; GlassCanonEvent agora reduz vida sem bonus de dano. Novos: randommob, ambush, horde, weakarms e heavy. Classes antigas permanecem no codigo por historico, mas nao participam do sorteio.

Roles hostis conferidas em Assets.zip 0.7.1: Spider, Wolf_Black, Skeleton_Fighter, Trork_Warrior e Toad_Rhino_Magma. NPCPlugin.getIndex valida disponibilidade em runtime. O spawn usa a API existente e referencias rastreadas; nao foi inventado um metodo de ataque/aggro. A IA e o terreno podem afetar o combate.

Controle MLG: Default.json da instalacao usa AirControlMinMultiplier=0, AirControlMinSpeed=0, AirControlMaxSpeed=3 e AirControlMaxMultiplier=3.13. Isso e compativel com a dificuldade de iniciar deslocamento horizontal em queda a partir do repouso; nao comprova sozinho toda a causa percebida pelo jogador. O efeito agora aplica controle nao nulo em toda a faixa e restaura precisamente cinco campos ao encerrar. Os testes verificam restauracao integral de MovementSettings sem alterar pulo, baseSpeed ou fly. Sensacao de controle e salvamento com balde ainda precisam de teste real.

Revisao posterior: poop foi retirado; agora ha 13 eventos cadastrados (12 ativos por padrao). ChallengeDeck substitui ChaosClock.choose no fluxo principal, sem reposicao durante toda a sessao. Ao esgotar, encerra em vez de repetir. HUD ganhou contador de desafios e altura 184. Alterar intervalo mantem o progresso da sequencia.

