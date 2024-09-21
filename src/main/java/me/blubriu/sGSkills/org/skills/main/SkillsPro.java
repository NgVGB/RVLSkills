package me.blubriu.sGSkills.org.skills.main;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import me.blubriu.sGSkills.org.skills.abilities.Ability;
import me.blubriu.sGSkills.org.skills.abilities.AbilityListener;
import me.blubriu.sGSkills.org.skills.abilities.devourer.DevourerGliders;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommandHandler;
import me.blubriu.sGSkills.org.skills.commands.TabCompleteManager;
import me.blubriu.sGSkills.org.skills.data.database.json.OldSkillsConverter;
import me.blubriu.sGSkills.org.skills.data.managers.CosmeticCategory;
import me.blubriu.sGSkills.org.skills.data.managers.DataHandlers;
import me.blubriu.sGSkills.org.skills.data.managers.PlayerDataManager;
import me.blubriu.sGSkills.org.skills.data.managers.backup.SkillsBackup;
import me.blubriu.sGSkills.org.skills.events.SkillsEventManager;
import me.blubriu.sGSkills.org.skills.gui.GUIConfig;
import me.blubriu.sGSkills.org.skills.gui.InteractiveGUIManager;
import me.blubriu.sGSkills.org.skills.main.locale.LanguageManager;
import me.blubriu.sGSkills.org.skills.managers.*;
import me.blubriu.sGSkills.org.skills.managers.blood.BloodManager;
import me.blubriu.sGSkills.org.skills.managers.blood.DamageAestheticsManager;
import me.blubriu.sGSkills.org.skills.managers.resurrect.LastBreath;
import me.blubriu.sGSkills.org.skills.masteries.managers.MasteryManager;
import me.blubriu.sGSkills.org.skills.services.ServiceWorldGuard;
import me.blubriu.sGSkills.org.skills.services.manager.ServiceHandler;
import me.blubriu.sGSkills.org.skills.types.Energy;
import me.blubriu.sGSkills.org.skills.types.SkillManager;
import me.blubriu.sGSkills.org.skills.types.Stat;
import me.blubriu.sGSkills.org.skills.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SkillsPro extends JavaPlugin {
    private static SkillsPro instance;
    private LanguageManager languageManager;
    private PlayerDataManager playerDataManager;
    private UpdateChecker updater;

    public static SkillsPro get() {
        return instance;
    }

    public static void main(String[] args) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, "An unbelievable unexpected unhandled IndexOutOfBoundsException has occurred:\n" +
                "The level is greater than the max value, resulting in an RException." +
                "\n\nThis is a Minecraft plugin.\nPut it in the plugins folder. Don't just click on it.", "RException", JOptionPane.ERROR_MESSAGE);

    }

    public LanguageManager getLang() {
        return languageManager;
    }

    @Override
    public void onLoad() {
        instance = this;
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) ServiceWorldGuard.init();
    }

    @Override
    public void onEnable() {
        FileManager manager = new FileManager(this);
        manager.createDataFolder();
        manager.loadConfig();

        languageManager = new LanguageManager(this);
        new GUIConfig(this);
        Stat.init(this);
        Energy.init(this);
        SkillManager.init(this);
        manager.setupWatchService();

        ServiceHandler.init(this);
        ServiceHandler.registerPlaceHolders();
        CosmeticCategory.load(this);

        PluginCommand cmd = this.getCommand("skills");
        cmd.setExecutor(new SkillsCommandHandler(this));
        cmd.setTabCompleter(new TabCompleteManager());

        updater = new UpdateChecker(this, 8981);
        registerAllEvents();

        new SkillsBackup(this);
        new Hologram(this);

        playerDataManager = new PlayerDataManager(this);
        new OldSkillsConverter(new File(getDataFolder(), "players"), this);
        playerDataManager.setTopLevels(this);

        updater.checkForUpdates().thenRun(updater::sendUpdates);
        new Metrics(this, 6224); // https://bstats.org/plugin/bukkit/SkillsPro/6224
        if (SkillsConfig.ARMOR_WEIGHTS_RESET_SPEEDS_ENABLED.getBoolean()) {
            // This prevents dangerous data losses.
            if (XReflection.supports(17))
                throw new IllegalStateException("Armor weight reset option is currently not supported on " + Bukkit.getVersion());
            OfflineNBT.perform();
        }
    }

    @Override
    public void onDisable() {
        if (playerDataManager == null) return;
        playerDataManager.saveAll();
        Hologram.onDisable();
        DevourerGliders.onDisable();
        Ability.onDisable();
    }

    public void reload() {
        languageManager = new LanguageManager(this);
    }

    private void registerEvent(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerAllEvents() {
        if (false) registerEvent(new ParticleUtil.Listen());
        registerEvent(new HealthAndEnergyManager(this));
        registerEvent(new AbilityListener());
        registerEvent(new InteractiveGUIManager());
        registerEvent(new SkillItemManager());
        registerEvent(new MoveManager());
        registerEvent(new XPAndEnchantmentManager());
        registerEvent(new SkillsEventManager());
        registerEvent(new DebugManager());
        registerEvent(new StatManager());
        if (SkillsConfig.LAST_BREATH_ENABLED.getBoolean() && XMaterial.supports(13)) registerEvent(new LastBreath());
        if (SkillsConfig.SMART_DAMAGE.getBoolean()) registerEvent(new DamageManager());
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean() || SkillsConfig.PULSE_ENABLED.getBoolean())
            registerEvent(new DamageAestheticsManager());
        if (SkillsConfig.BLOOD_ENABLED.getBoolean()) registerEvent(new BloodManager());

        registerEvent(new LevelManager(this));
        if (SkillsMasteryConfig.MASTERIES_ENABLED.getBoolean()) new MasteryManager();
        if (SkillsConfig.ARMOR_WEIGHTS_ENABLED.getBoolean()) registerEvent(new ArmorWeights());
        registerEvent(new DataHandlers(this));
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public UpdateChecker getUpdater() {
        return updater;
    }
}