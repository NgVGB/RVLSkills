package me.blubriu.sGSkills.org.skills.masteries.finesse;

import org.bukkit.event.EventHandler;
import me.blubriu.sGSkills.org.skills.api.events.SkillSoulGainEvent;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.masteries.managers.Mastery;

public class MasteryReap extends Mastery {
    public MasteryReap() {
        super("Reap", true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKill(SkillSoulGainEvent event) {
        SkilledPlayer info = this.checkup(event.getPlayer());
        if (info == null) return;
        event.setGained((int) (event.getGained() + getScaling(info)));
    }
}
