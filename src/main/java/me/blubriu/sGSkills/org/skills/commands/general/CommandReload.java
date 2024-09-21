package me.blubriu.sGSkills.org.skills.commands.general;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommand;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommandHandler;
import me.blubriu.sGSkills.org.skills.data.managers.CosmeticCategory;
import me.blubriu.sGSkills.org.skills.gui.GUIConfig;
import me.blubriu.sGSkills.org.skills.main.FileManager;
import me.blubriu.sGSkills.org.skills.main.SkillsConfig;
import me.blubriu.sGSkills.org.skills.main.SkillsMasteryConfig;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;
import me.blubriu.sGSkills.org.skills.managers.HealthAndEnergyManager;
import me.blubriu.sGSkills.org.skills.managers.LevelManager;
import me.blubriu.sGSkills.org.skills.masteries.managers.MasteryManager;
import me.blubriu.sGSkills.org.skills.types.Energy;
import me.blubriu.sGSkills.org.skills.types.SkillManager;
import me.blubriu.sGSkills.org.skills.types.Stat;
import me.blubriu.sGSkills.org.skills.utils.Hologram;

public class CommandReload extends SkillsCommand {
    public CommandReload() {
        super("reload", SkillsLang.COMMAND_RELOAD_DESCRIPTION, false);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        FileManager.created = false;
        FileManager.isNew = false;
        FileManager manager = new FileManager(plugin);

        manager.createDataFolder();
        manager.loadConfig();
        plugin.reload();
        if (args.length > 0 && args[0].equalsIgnoreCase("config")) {
            SkillsLang.Command_Reload_Done.sendMessage(sender);
            return;
        }

        new GUIConfig(plugin); // Needs to be before loading cosmetics or it resetes the GUIs
        CosmeticCategory.load(plugin);
        new SkillsCommandHandler(plugin);
        Hologram.load();
        Stat.init(plugin);
        Energy.init(plugin);

        LevelManager.load(plugin);
        SkillManager.init(plugin);

        MasteryManager.getMasteries().forEach(HandlerList::unregisterAll);
        if (SkillsMasteryConfig.MASTERIES_ENABLED.getBoolean()) new MasteryManager();

        for (Player players : Bukkit.getOnlinePlayers()) {
            if (!SkillsConfig.isInDisabledWorld(players.getLocation()))
                HealthAndEnergyManager.updateStats(players);
        }

        plugin.getPlayerDataManager().saveAll();
        SkillsLang.Command_Reload_Done.sendMessage(sender);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length > 1) return new String[0];
        return new String[]{"config"};
    }
}
