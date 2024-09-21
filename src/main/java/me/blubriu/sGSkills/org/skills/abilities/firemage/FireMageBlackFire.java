package me.blubriu.sGSkills.org.skills.abilities.firemage;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import me.blubriu.sGSkills.org.skills.abilities.Ability;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.utils.EntityUtil;

public class FireMageBlackFire extends Ability {
    public FireMageBlackFire() {
        super("FireMage", "black_fire");
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK && event.getCause() != EntityDamageEvent.DamageCause.FIRE)
            return;

        for (Entity entity : event.getEntity().getNearbyEntities(5, 5, 5)) {
            if (EntityUtil.isInvalidEntity(entity)) continue;
            if (!(entity instanceof Player)) continue;

            Player player = (Player) entity;
            SkilledPlayer info = this.checkup(player);
            if (info == null) continue;

            double damage = this.getScaling(info, "damage");
            event.setDamage(event.getDamage() + damage);
            Location location = event.getEntity().getLocation();
            player.getWorld().playEffect(location, Effect.STEP_SOUND, Material.COAL_BLOCK);
        }
    }
}
