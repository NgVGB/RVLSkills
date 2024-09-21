package me.blubriu.sGSkills.org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommand;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;

public class CommandAbout extends SkillsCommand {
    public static final String USER = "%%__USER__%%";
    public static final String NONCE = "%%__NONCE__%%";

    public CommandAbout() {
        super("about", SkillsLang.COMMAND_ABOUT_DESCRIPTION, false, "version");
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        String register = USER.startsWith("%%__USER__%") && USER.endsWith("%%") ? "&4Unrecognized" :
                "&9" + USER + " &8(&2" + NONCE + "&8)";
        MessageHandler.sendMessage(sender, "\n&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n\n" +
                "             &7[ &bAsakaRVLSkills &7]\n" +
                "&7| &2" + plugin.getDescription().getDescription() + '\n' +
                "&7| &fDevelopers&8: &9Crypto Morin, Hex_26 & BlubRiu\n" +
                "&7| &fVer&8: &9" + plugin.getDescription().getVersion() + '\n' +
                "&7| &fReg&8: " + register + "\n\n" +
                "&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n ");
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}
