package me.rcbaito.hytale.hud;
/** Portuguese copy for the challenge-only event pool. */
public record EventText(String name, String line1, String line2) {
    public static EventText forId(String id, String fallbackName) {
        return switch (id) {
            case "mlg" -> new EventText("Desafio MLG", "Queda de 300 blocos! Use WASD no ar", "e o balde para tentar se salvar.");
            case "poop" -> new EventText("Inventário Entupido", "100 cocôs vão para seu inventário.", "Mais bagunça para administrar!");
            case "spiderrain" -> new EventText("Chuva de Aranhas", "Aranhas hostis caem perto de você.", "Não fique parado!");
            case "toadsquad" -> new EventText("Esquadrão de Sapos", "Sapos de magma surgem por perto.", "Prepare-se para o combate!");
            case "glasscanon" -> new EventText("Vida de Vidro", "Você recebe três vezes mais dano.", "Sua vida máxima continua normal.");
            case "pacifist" -> new EventText("Pacifista", "Seu dano fica zerado", "durante o efeito. Tente fugir!");
            case "snail" -> new EventText("Modo Caracol", "Você fica muito mais lento", "durante o efeito.");
            case "cement" -> new EventText("Sapatos de Cimento", "Você não consegue pular", "enquanto o efeito estiver ativo.");
            case "randommob" -> new EventText("Visitante Indesejado", "Um mob hostil aleatório surge perto.", "O tipo sorteado aparece no chat.");
            case "ambush" -> new EventText("Emboscada", "Três mobs hostis aleatórios surgem.", "Você foi cercado!");
            case "horde" -> new EventText("Horda Aleatória", "Cinco inimigos aleatórios por perto.", "Sobreviva à horda!");
            case "weakarms" -> new EventText("Braços Fracos", "Você causa só 25% do dano normal.", "Escolha suas brigas com cuidado!");
            case "heavy" -> new EventText("Peso Morto", "Você anda devagar e pula baixo.", "Fugir ficou bem mais difícil!");
            case "kill" -> new EventText("Morte Instantânea", "Sua vida é zerada imediatamente.", "Ativado manualmente na configuração.");
            default -> new EventText(fallbackName, "Um novo desafio começou!", "");
        };
    }
}

