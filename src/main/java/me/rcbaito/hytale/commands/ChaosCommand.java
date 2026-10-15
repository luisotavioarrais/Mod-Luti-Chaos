package me.rcbaito.hytale.commands;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.rcbaito.hytale.plugin.*;
public final class ChaosCommand extends AbstractCommandCollection {
    public ChaosCommand() {
        super("chaos", "Luti Chaos"); requirePermission("luti.chaos.manage");
        for (String name : new String[]{"start","stop","status","help","events","add","remove","interval"}) addSubCommand(new Action(name));
    }
    private static final class Action extends AbstractPlayerCommand {
        private final String action;
        private RequiredArg<PlayerRef> target;
        private RequiredArg<Integer> interval;
        Action(String action) {
            super(action, "Luti Chaos: " + action); this.action = action;
            requirePermission("luti.chaos.manage");
            if (action.equals("add") || action.equals("remove")) target = withRequiredArg("Nome", "Participante", ArgTypes.PLAYER_REF);
            if (action.equals("interval")) interval = withRequiredArg("segundos", "5 a 3600", ArgTypes.INTEGER);
        }
        @Override protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef sender, World world) {
            if (ChaosGameManager.currentWorld != null && ChaosGameManager.currentWorld != world && ChaosGameManager.isRunning) {
                sender.sendMessage(Message.raw("Pare o desafio no mundo de origem antes de iniciar em outro mundo.")); return;
            }
            if (!ChaosGameManager.isRunning && ChaosGameManager.currentWorld != world) {
                ChaosGameManager.affectedPlayers.clear();
                ChaosGameManager.currentWorld = world; ChaosGameManager.currentStore = store;
            }
            String message;
            switch (action) {
                case "start" -> {
                    if (ChaosGameManager.isRunning) message = "LUTI CHAOS ja esta ativo.";
                    else { ChaosGameManager.addPlayer(sender); ChaosGameManager.startLoop(); message = "LUTI CHAOS iniciado. Primeiro evento em " + ChaosGameManager.maxChaosTime + "s."; }
                }
                case "stop" -> { ChaosGameManager.stopLoop(); message = "LUTI CHAOS parado; efeitos temporarios removidos."; }
                case "status" -> message = "LUTI CHAOS: " + (ChaosGameManager.isRunning ? "ativo" : "parado") + " | " + ChaosGameManager.chaosTime + "s | Desafio " + ChaosGameManager.challengeNumber + "/" + ChaosGameManager.challengeTotal + " | " + ChaosGameManager.lastEvent;
                case "interval" -> {
                    int seconds = interval.get(ctx);
                    if (seconds < 5 || seconds > 3600) message = "Use um intervalo entre 5 e 3600 segundos.";
                    else { ChaosGameManager.changeInterval(seconds); message = "Intervalo: " + seconds + "s (sessao atual)."; }
                }
                case "add" -> { var p = target.get(ctx); if (!ChaosGameManager.belongs(p)) message = "O participante precisa estar neste mundo."; else { ChaosGameManager.addPlayer(p); message = "Adicionado: " + p.getUsername(); } }
                case "remove" -> { var p = target.get(ctx); ChaosGameManager.removePlayer(p); message = "Removido: " + p.getUsername(); }
                case "events" -> { for (var e : ChaosEventRegistry.getEvents()) sender.sendMessage(Message.raw(e.getId() + " | " + e.getName() + " | " + ChaosGameManager.eventState(e))); return; }
                default -> message = "/chaos start | stop | interval 30 | add Nome | remove Nome | events | status | help";
            }
            sender.sendMessage(Message.raw(message));
        }
    }
}

