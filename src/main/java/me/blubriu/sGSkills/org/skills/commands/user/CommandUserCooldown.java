package me.blubriu.sGSkills.org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import me.blubriu.sGSkills.org.skills.abilities.AbilityManager;
import me.blubriu.sGSkills.org.skills.commands.SkillsCommand;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;

public class CommandUserCooldown extends SkillsCommand {
    public CommandUserCooldown(SkillsCommand group) {
        super("cooldown", group, SkillsLang.COMMAND_USER_COOLDOWN_DESCRIPTION, false);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            if (!offlinePlayer.hasPlayedBefore()) {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
                return;
            }

            if (AbilityManager.getCooldownExcempts().remove(offlinePlayer.getUniqueId())) {
                SkillsLang.COMMAND_USER_COOLDOWN_DISABLED.sendMessage(sender, "%name%", args[0]);
            } else {
                SkillsLang.COMMAND_USER_COOLDOWN_ENABLED.sendMessage(sender, "%name%", args[0]);
                AbilityManager.getCooldownExcempts().add(offlinePlayer.getUniqueId());
            }
            return;
        }

        CommandUser.handle(this, sender, args, (changeFactory, player, info, silent) -> {
            double pre = info.getCooldownTimeLeft();
            int finalAmount = (int) changeFactory.withInitialAmount(pre).getFinalAmount();

            if (silent) {
                info.setCooldown(finalAmount);
            } else {
                long decreaseTime = (long) (pre - changeFactory.getAmount());

                if (decreaseTime < 0) {
                    info.setCooldown(0);
                }

                info.setCooldown(
                        changeFactory.getType() == AmountChangeFactory.Type.REMOVE
                                ? decreaseTime
                                : finalAmount
                );
            }

            return true;
        });
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount in ms>"};
        if (args.length == 4) return new String[]{"<raw>"};
        return new String[0];
    }
}