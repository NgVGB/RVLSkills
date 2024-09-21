package me.blubriu.sGSkills.org.skills.masteries.brutality;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import me.blubriu.sGSkills.org.skills.api.events.SkillXPGainEvent;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.masteries.managers.Mastery;

public class MasteryGlory extends Mastery {
    public MasteryGlory() {
        super("Glory", true);
    }

    @EventHandler
    public void onDeath(SkillXPGainEvent event) {
        Player player = event.getPlayer();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        double gained = event.getGained();
        event.setGained(gained + getScaling(info, "%xp%", gained));
    }
}