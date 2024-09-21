package me.blubriu.sGSkills.org.skills.abilities.priest;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import me.blubriu.sGSkills.org.skills.abilities.AbilityContext;
import me.blubriu.sGSkills.org.skills.abilities.InstantActiveAbility;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.utils.versionsupport.VersionSupport;

import java.util.List;

public class PriestKindlingOfLife extends InstantActiveAbility {
    private static final String SPELL = "PRIEST_SPELL";

    public PriestKindlingOfLife() {
        super("Priest", "kindling_of_life");
        setPvPBased(false);
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();
        int lvl = info.getAbilityLevel(this);

        if (player.isSneaking()) {
            XSound.ENTITY_GENERIC_DRINK.play(player.getLocation());
            ParticleDisplay.of(XParticle.HEART).withLocation(player.getLocation()).withCount(lvl * 20).offset(0.5, 0.5, 0.5).spawn();
            VersionSupport.heal(player, this.getScaling(info, "heal"));
            return;
        }

        Vector vector = player.getEyeLocation().getDirection().multiply(lvl * 2);
        Projectile projectile = player.launchProjectile(ThrownPotion.class, vector);
        projectile.setMetadata(SPELL, new FixedMetadataValue(SkillsPro.get(), lvl));
/*        Laser laser = null;
        try {
            laser = new Laser(player.getLocation(), projectile.getLocation(), 100, 100);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        laser.start(Skills.get());*/
        XSound.ENTITY_SPLASH_POTION_THROW.play(player);

        // Laser finalLas = laser;
        new BukkitRunnable() {
            int i = 5;

            @Override
            public void run() {
                ParticleDisplay.of(XParticle.HEART).withCount(10).offset(0.01).withExtra(0.1).spawn(projectile.getLocation());
//                try {
//                    finalLas.moveEnd(projectile.getLocation());
//                } catch (ReflectiveOperationException e) {
//                    e.printStackTrace();
//                }
                if (--i == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 5);

        XSound.ITEM_FIRECHARGE_USE.play(player);
    }

    @EventHandler
    public void spellHit(ProjectileHitEvent event) {
        Entity hit = event.getHitEntity();
        if (hit == null || isInvalidTarget(hit)) return;
        ProjectileSource shooter = event.getEntity().getShooter();
        if (shooter == null) return;
        if (!event.getEntity().hasMetadata(SPELL)) return;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) shooter);
        LivingEntity livingEntity = (LivingEntity) hit;

        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play((Entity) shooter, 2, 0);
        hit.getWorld().spawnParticle(XParticle.HEART.get(), hit.getLocation(), 30, 0.5, 0.5, 0.5, 0.5);

        List<PotionEffect> effects;
        if (hit instanceof Player) effects = getEffects(info, "effects");
        else effects = getEffects(info, "debuffs");
        ((LivingEntity) hit).addPotionEffects(effects);

        VersionSupport.heal(livingEntity, this.getScaling(info, "heal"));
    }
}
