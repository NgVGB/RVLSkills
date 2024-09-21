package me.blubriu.sGSkills.org.skills.commands;

import com.cryptomorin.xseries.XSound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import me.blubriu.sGSkills.org.skills.commands.bonuses.CommandBonus;
import me.blubriu.sGSkills.org.skills.commands.events.CommandEvent;
import me.blubriu.sGSkills.org.skills.commands.general.*;
import me.blubriu.sGSkills.org.skills.commands.user.CommandUser;
import me.blubriu.sGSkills.org.skills.data.managers.CosmeticCategory;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;
import me.blubriu.sGSkills.org.skills.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SkillsCommandHandler implements CommandExecutor {
    protected static final List<SkillsCommand> commands = new ArrayList<>();
    private final SkillsPro plugin;

    public SkillsCommandHandler(SkillsPro plugin) {
        this.plugin = plugin;
        commands.clear();
        // Sub commands should not be registered here.

        new CommandHelp();
        new CommandUser();
        new CommandUpdates();
        new CommandAbout();
        new CommandDebug();

        new CommandSelect();
        new CommandInfo();
        new CommandTop();
        new CommandHPScaling();
        new CommandStatistics();
        new CommandSound();
        new CommandTest();
        new CommandStats();
        new CommandShowActiveMessages();
        new CommandActionBar();
        new CommandReload();
        new CommandRefresh();
        new CommandMastery();
        new CommandImprove();
        new CommandKeyBinding();

        new CommandBonus();
        new CommandEvent();
        new CommandSouls();
        new CommandShop();
        new CommandMusic();

        for (CosmeticCategory category : CosmeticCategory.getCategories().values()) new CommandCosmetic(category);
    }

    public static List<SkillsCommand> getCommands() {
        return commands;
    }

    public static SkillsCommand getSmartCommand(String[] args, int cmdIndex) {
        String[] groups = Arrays.copyOfRange(args, cmdIndex - 2, args.length);
        return getCommand(args[cmdIndex - 1], groups);
    }

    public static SkillsCommand getCommand(String name, String... groups) {
        if (groups.length == 0)
            return commands.stream().filter(c -> c.getGroup() == null && c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        SkillsCommand lastGroup = null;
        for (String group : groups) {
            SkillsCommand finalInnerGroup = lastGroup;
            Stream<SkillsCommand> groupCmdStream = lastGroup == null ?
                    commands.stream().filter(c -> c.isGroup() && c.getName().equalsIgnoreCase(group)) :
                    commands.stream().filter(c -> c.isGroup() && c.getName().equalsIgnoreCase(group) && finalInnerGroup.equals(c.getGroup()));
            SkillsCommand groupCmd = groupCmdStream.findFirst().orElse(null);

            if (groupCmd == null) return null;
            lastGroup = groupCmd;
        }
        SkillsCommand finalGroup = lastGroup;
        return commands.stream().filter(c -> c.getGroup().equals(finalGroup)).findFirst().orElse(null);
    }

    public static boolean isCommand(String name) {
        return commands.stream().anyMatch(c -> c.matchesNames(name));
    }

    public static void executeHelperForGroup(SkillsCommand command, CommandSender sender) {
        List<SkillsCommand> subCommands = Arrays.asList(command.getSubCommands());
        SkillsCommand[] scs = subCommands.stream().filter(c -> c.hasPermission(sender)).toArray(SkillsCommand[]::new);

        SkillsLang.COMMAND_HELP_GROUPED_HEADER.sendMessage(sender, "%group%", StringUtils.capitalize(command.getName()));
        for (SkillsCommand cmd : scs) {
            StringBuilder name = new StringBuilder(cmd.getName());
            SkillsCommand group = cmd.getGroup();

            while (group != null) {
                name.insert(0, group.getName() + ' ');
                group = group.getGroup();
            }

            SkillsLang.COMMAND_HELP_GROUPED_COMMANDS.sendMessage(sender, "%cmd%", name, "%description%", cmd.getDescription());
        }
    }

    public static void sendUsage(CommandSender sender, String cmd) {
        MessageHandler.sendMessage(sender, SkillsLang.Command_Usage.parse() + cmd);
    }

    public static void sendNotNumber(CommandSender sender, String needed, String arg) {
        SkillsLang.Command_Not_Number.sendMessage(sender, "%needed%", needed, "%arg%", arg);
    }

    protected static Pair<SkillsCommand, Integer> getLastCommand(String[] args) {
        if (args.length == 1) return new Pair<>(commands.stream()
                .filter(c -> c.getGroup() == null && c.matchesNames(args[0]))
                .findFirst().orElse(null), 0);

        SkillsCommand lastCmd = null;
        int i = 0;
        while (i < args.length) {
            String group = args[i];
            SkillsCommand finalInnerGroup = lastCmd;
            Stream<SkillsCommand> groupCmdStream = lastCmd == null ?
                    commands.stream().filter(c -> c.matchesNames(group) && c.getGroup() == null) :
                    commands.stream().filter(c -> c.matchesNames(group) && finalInnerGroup.equals(c.getGroup()));
            SkillsCommand groupCmd = groupCmdStream.findFirst().orElse(null);

            if (groupCmd == null) {
                i--;
                break;
            }
            lastCmd = groupCmd;
            i++;
        }

        return new Pair<>(lastCmd, i);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String[] args) {
        if (args.length == 0) {
            SkillsCommand help = getCommand("help");
            help.plugin = this.plugin;
            help.runCommand(sender, args);
            return true;
        }

        Pair<SkillsCommand, Integer> pair = getLastCommand(args);
        SkillsCommand command = pair.getKey();
        int cmdIndex = pair.getValue();

        if (command == null) {
            SkillsLang.Command_Unknown.sendMessage(sender);
            if (sender instanceof Player) XSound.BLOCK_NOTE_BLOCK_BASS.play((Player) sender);
            return false;
        }

        if (command.hasPermission(sender)) {
            command.plugin = this.plugin;
            String[] cmdArgs = Arrays.stream(args).skip(cmdIndex + 1).toArray(String[]::new);
            command.runCommand(sender, cmdArgs);
            return true;
        } else {
            SkillsLang.PERMISSION.sendMessage(sender);
            XSound.BLOCK_NOTE_BLOCK_BASS.play((Player) sender);
            return false;
        }
    }

    protected static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}