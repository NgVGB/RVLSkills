package me.blubriu.sGSkills.org.skills.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.blubriu.sGSkills.org.skills.data.managers.PlayerDataManager;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.managers.HealthAndEnergyManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Deprecated
public final class SkillsAPI {
    private static final PlayerDataManager MANAGER = SkillsPro.get().getPlayerDataManager();

    @Nonnull
    public static CompletableFuture<List<UUID>> getTopLevels(int size) {
        return PlayerDataManager.getTopLevels(size);
    }

    @Nonnull
    public static SkilledPlayer getSkilledPlayer(OfflinePlayer player) {
        return getSkilledPlayer(player.getUniqueId());
    }

    public static SkilledPlayer getSkilledPlayer(UUID id) {
        return SkilledPlayer.getSkilledPlayer(id);
    }

    public static void deletePlayerData(OfflinePlayer player) {
        MANAGER.delete(player.getUniqueId());
    }

    public static void resetPlayerData(OfflinePlayer player) {
        MANAGER.delete(player.getUniqueId());
    }

    public static void updateStats(Player player) {
        HealthAndEnergyManager.updateStats(player);
    }
}