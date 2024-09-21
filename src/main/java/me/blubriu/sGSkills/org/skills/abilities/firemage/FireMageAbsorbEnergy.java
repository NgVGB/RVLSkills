package me.blubriu.sGSkills.org.skills.abilities.firemage;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import me.blubriu.sGSkills.org.skills.abilities.Ability;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.utils.MathUtils;

public class FireMageAbsorbEnergy extends Ability {
    public FireMageAbsorbEnergy() {
        super("FireMage", "absorb_energy");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireMageAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        int chance = (int) this.getScaling(info, "chance");
        if (!MathUtils.hasChance(chance)) return;

        int lvl = info.getAbilityLevel(this);
        Entity entity = event.getEntity();
        Location loc = entity.getLocation();

        entity.setFireTicks((int) (entity.getFireTicks() + (getScaling(info, "fire", event) * 20)));
        ParticleDisplay display = ParticleDisplay.of(XParticle.FLAME).withLocation(loc).withExtra(.2);

        if (lvl == 1) {
            display.withCount(chance / 2).offset(.5).spawn();
        } else {
            XSound.ITEM_FIRECHARGE_USE.play(entity);
            if (lvl == 2) {
                display.withCount((chance / 2) + 10).offset(.3).spawn();
            } else {
                Particles.helix(SkillsPro.get(), 3, 0.7, 0.1, 1, 5, 1, 0.5, false, false, ParticleDisplay.of(XParticle.FLAME).withLocation(loc).withCount(2));
            }
        }
    }
}
