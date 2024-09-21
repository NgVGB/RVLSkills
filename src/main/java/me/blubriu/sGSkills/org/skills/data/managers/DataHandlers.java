package me.blubriu.sGSkills.org.skills.data.managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import me.blubriu.sGSkills.org.skills.data.database.DataContainer;
import me.blubriu.sGSkills.org.skills.data.database.SkillsDatabase;
import me.blubriu.sGSkills.org.skills.data.database.json.JsonDatabase;
import me.blubriu.sGSkills.org.skills.main.SkillsConfig;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;

import java.io.File;

public class DataHandlers implements Listener {
    private final SkillsPro plugin;

    public DataHandlers(SkillsPro plugin) {
        this.plugin = plugin;
    }

    public static <T extends DataContainer> SkillsDatabase<T> getDatabase(SkillsPro plugin, String folder, Class<T> adapter) {
        String db = SkillsConfig.DATABASE.getString().toLowerCase();

        if (db.equals("json")) return new JsonDatabase<>(new File(plugin.getDataFolder(), folder), adapter);

        MessageHandler.sendConsolePluginMessage("&4Invalid database type&8: &e" + db);
        MessageHandler.sendConsolePluginMessage("&4Disabling the plugin...");
        plugin.onDisable();
        return null;
    }

    @EventHandler
    public void onLeaveEvent(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().save(SkilledPlayer.getSkilledPlayer(event.getPlayer()));
        plugin.getPlayerDataManager().unload(event.getPlayer().getUniqueId());
    }
}
