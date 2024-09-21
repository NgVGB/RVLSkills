package me.blubriu.sGSkills.org.skills.abilities.firemage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import me.blubriu.sGSkills.org.skills.abilities.Ability;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.utils.versionsupport.VersionSupport;

public class FireMagePassive extends Ability {
    public FireMagePassive() {
        super("FireMage", "passive");
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireDamage(EntityDamageEvent event) {
        switch (event.getCause()) {
            case FIRE_TICK:
            case FIRE:
            case LAVA:
                break;
            default:
                return;
        }
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE)
            VersionSupport.heal(player, this.getScaling(info, "heal"));
        event.setCancelled(true);
    }
}