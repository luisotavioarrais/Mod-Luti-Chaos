# Validacao — 14/10/2026

## Resultados

- Gradle Wrapper 9.2.0, JDK Eclipse Temurin 25.0.4.1+1.
- `gradlew.bat clean dist`: compilacao, empacotamento e testes concluidos com BUILD SUCCESSFUL.
- JUnit: 7 testes, 0 falhas, 0 erros, 0 ignorados.
- Casos: prazo real de 30s; atraso sem rajada; 10.000 sorteios sem repeticao consecutiva; nenhum candidato/um candidato; configuracao padrao; validacao de limites e switches; opt-in de Instant Death.
- Boot isolado com HytaleServer.jar 0.7.1 e Assets.zip instalados, autenticacao offline, bind somente 127.0.0.1:15520, sem clientes e sem saves existentes.
- Servidor habilitou `LutiPlay:LutiChaos`, registrou componentes/sistemas, reconheceu `chaos help` e encerrou com `stop`.
- Como executado pelo console, `chaos help` respondeu que exige jogador. Isso confirma resolucao do comando; nao valida suas acoes com jogador.
- O primeiro ensaio --bare teve erro do proprio ServerManager no shutdown sem transport. Foi substituido pelo boot completo; o ensaio completo final concluiu o shutdown.
- O ensaio completo intermediario detectou manifesto invalido para bare version 0.7.1; corrigido para =0.7.1 e repetido com sucesso.
- Logs do servidor ainda contêm avisos de assets nativos e mensagens SERR Reallocate; nao foram tratados como validacao dos eventos do mod.

Arquivos de evidencia entregues em `dist/validation/`: log Gradle, XML JUnit e log do boot completo final. SHA256 do artefato em `dist/SHA256SUMS.txt`.

## Roteiro ainda pendente no cliente

Use primeiro uma copia de mundo, sem Random Chaos original.

1. Habilitar Luti Chaos e entrar com o proprietario/operador. Confirmar `/chaos help` e permissoes; confirmar que jogador sem permissoes nao administra o desafio.
2. `/chaos start`: verificar HUD 30 ate 0, barra, aviso aos 5s, primeiro evento aproximadamente 30s depois e nome do evento.
3. Repetir start varias vezes: observar apenas um sorteio a cada intervalo. Deixar rodar pelo menos dez rodadas, sem repeticao consecutiva.
4. `/chaos stop` entre 1s e 0s: confirmar ausencia de novos eventos e restauracao de velocidade, pulo, dano, saude maxima, modo de jogo e NPCs.
5. `/chaos interval 5`, status, add/remove e intervalo invalido: verificar limites, reinicio da contagem e limpeza.
6. Para ensaiar cada evento isoladamente, habilitar apenas seu ID na configuracao e reabrir o mundo. Com um unico evento, ha uma rodada vazia entre ocorrencias para nao repetir consecutivamente.
7. Confirmar gameplay de cada um dos 17 eventos da tabela do README; para morte instantanea, ativar explicitamente allowInstantDeath. Verificar rifle/municao, balde e aterrissagem MLG.
8. Fechar/reabrir mundo durante Sonic, Super Jump, Creative e Chicken Rain; conferir restauracao, ausencia de timer antigo e progresso preservado. Repetir ao trocar de mundo, desconectar e morrer.
9. Testar maxEntitiesPerEvent=0 e 4; com varios participantes, conferir que o limite e total por evento.
10. Validar renderizacao do .ui na resolucao usada para gravacao e coexistencia com HUDs de outros mods.

Esses testes nao foram executados com um cliente. Compilacao e boot nao justificam afirmar que o mod esta totalmente funcional.

## Correcao apos teste com jogador — HUD

O cliente desconectou em `/chaos start`, informando: CustomUI Set command selector doesn't match a markup property. Selector: #ChaosProgressBar.Anchor.Width.

Corrigido para enviar #ChaosProgressBar.Anchor como objeto Anchor completo (Width e Height), usando setObject. Novo teste serializa os comandos reais da HUD com 30, 15, 5 e 0 segundos e verifica o objeto emitido. Total atualizado: 8 testes, 0 falhas. O teste exige HytaleLogManager configurado antes de inicializar classes da API; configuracao incluida no Gradle.

O JAR corrigido foi copiado para UserData/Mods e seu SHA256 foi comparado ao artefato de dist. A confirmacao visual da correcao ainda requer reabrir o Hytale e repetir `/chaos start`. O log de boot entregue originalmente se refere a compilacao anterior a esta correcao de HUD.

## Nomes e descricoes em portugues
Adicionados nome traduzido e duas linhas curtas de descricao para os 17 eventos. A HUD mantem os textos durante a rodada e o chat tambem anuncia a descricao. Painel ampliado para 300 x 164. Build e oito testes passaram; disposicao visual deve ser conferida no cliente. JAR instalado atualizado com hash confirmado.


## Revisao somente desafios e controle aereo MLG

Build com 10 testes passando. Novos casos verificam que eventos beneficos nao entram no registro mesmo com configuracao antiga e que o controle aereo restaura os valores originais sem alterar voo/pulo. A tabela atual de 14 eventos esta no README e substitui a lista anterior.

Pendente no cliente: confirmar ataque e posicionamento dos cinco tipos de mob, numero total gerado respeitando limite, retirada dos bonus antigos e deslocamento WASD durante a queda MLG (inclusive partindo parado). Confirmar restauracao apos stop, troca de mundo e morte. Os testes de servidor nao reproduzem a fisica/input do cliente.

## Sequencia finita sem repeticao e contador

Evento poop removido do registro: 13 cadastrados, 12 ativos com Instant Death bloqueado por padrao. Cada evento ativo e retirado de uma lista embaralhada por sorteio e nao retorna nessa sessao. Apos a ultima rodada, efeitos sao limpos e o desafio encerra com mensagem de conclusao. Novo start inicia outra sequencia. Interval nao reinicia a lista sorteada. HUD/chat/status mostram o numero atual e total; o denominador respeita configuracao e eventos invalidados antes de sair.

Build: 13 testes, sem falhas. Novos testes exercitam 100 sequencias completas de 12 entradas, ausencia de repeticao ate esgotar, encerramento sem reposicao, filtro de eventos desativados, reset e lista vazia. Confirmar no cliente a nova linha Desafio N/total e mensagem de conclusao depois da ultima rodada. Os testes anteriores de escolha sem repeticao consecutiva cobrem apenas o helper legado, que nao e mais usado pelo sorteio principal.

## Controle MLG encerrado na aterrissagem

Corrigida reaplicacao de controle aereo a cada tick de evento. Agora e aplicado somente no start; o pulse de 100ms acompanha MovementStates apos o teleporte e restaura ao detectar aterrissagem, agua, escalada ou novo pulo depois da queda. Morte e timeout de 20 segundos tambem encerram o ajuste. Stop e remocao de jogador continuam restaurando. Novo pulo nao reativa o ajuste.

Build e 16 testes passaram, incluindo tres testes novos de estado: ignorar chao antes de iniciar a queda, restaurar ao aterrissar, detectar novo pulo e entrada na agua. Confirmar no cliente que os pulos apos a queda voltaram ao movimento normal. A observacao de aterrissagem usa o estado informado pelo cliente e amostragem de 100ms.

## Correcao de fragilidade e morte

Vida de Vidro nao altera mais saude maxima: usa FragileComponent e multiplica por 3 o dano recebido pelo pipeline normal. Ao morrer, efeitos ativos daquele participante sao encerrados e removidos; ele permanece participante para depois do respawn. resetStat agora apenas remove a chave pertencente ao mod, sem maximizar saude ou clonar/substituir EntityStatMap. Na entrada do jogador, glass_hp e tank_hp legados sao removidos, se presentes. Nao se remove DeathComponent nem se tenta reviver um jogador por escrita direta na saude.

17 testes passaram; teste novo cobre a passagem de dano normal/fragil e dano zero. Combate, morte, animacao e respawn ainda precisam de teste no cliente; nao se afirma que um personagem ja travado tenha sido recuperado. Reabrir mundo carrega a limpeza de modificadores legados.
