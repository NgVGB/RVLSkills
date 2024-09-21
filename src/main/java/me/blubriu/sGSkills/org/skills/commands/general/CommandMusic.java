package me.blubriu.sGSkills.org.skills.commands.general;

import com.cryptomorin.xseries.NoteBlockMusic;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kingdoms.main.Kingdoms;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommand;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;

import java.util.Locale;

public class CommandMusic extends SkillsCommand {
    public CommandMusic() {
        super("music", SkillsLang.COMMAND_MUSIC_DESCRIPTION, false, "sing");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
            return;
        }
        if (args.length == 0) {
            SkillsLang.COMMAND_MUSIC_USAGE.sendMessage(sender);
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.getSkillName().toLowerCase(Locale.ENGLISH).contains("priest")) {
            SkillsLang.COMMAND_MUSIC_PRIEST_ONLY.sendMessage(sender);
            return;
        }

        SkillsLang.COMMAND_MUSIC_PLAYING.sendMessage(sender);
        Bukkit.getScheduler().runTaskAsynchronously(Kingdoms.get(), () -> {
            try {
                NoteBlockMusic.Sequence instructions = NoteBlockMusic.parseInstructions(String.join(" ", args));
                instructions.play(player, player::getLocation, true);
                SkillsLang.COMMAND_MUSIC_DONE.sendMessage(sender);
            } catch (Throwable ex) {
                SkillsLang.COMMAND_MUSICT_ERROR.sendMessage(sender, "%error%", ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[]{MessageHandler.colorize("&2<instrument>&8,&6<note>&8,&9<repeat>&8,&5<repeatFermata> &7<Fermata>")};
    }
}
