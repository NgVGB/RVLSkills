package me.blubriu.sGSkills.org.skills.commands.general;

import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommand;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommandHandler;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;

import java.util.Arrays;
import java.util.Locale;

public class CommandSound extends SkillsCommand {
    public CommandSound() {
        super("sound", null, SkillsLang.COMMAND_SOUND_DESCRIPTION, false, "play", "testsound");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
            return;
        }
        if (args.length == 0) {
            SkillsCommandHandler.sendUsage(sender, "<sound> [volume] [pitch]");
            return;
        }
        MessageHandler.sendMessage(sender, "&3Playing sound...");
        XSound.play(String.join(",", args), x -> x.forPlayers((Player) sender));
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) return new String[0];
        if (args.length == 1) return Arrays.stream(XSound.VALUES).map(Enum::name).filter(x
                -> x.contains(args[0].toUpperCase(Locale.ENGLISH))).toArray(String[]::new);
        if (args.length == 2) return new String[]{"[volume 0.0-∞]"};
        if (args.length == 3) return new String[]{"[pitch 0.5-2.0]"};
        return new String[0];
    }
}
