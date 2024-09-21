package me.blubriu.sGSkills.org.skills.abilities.mage;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import me.blubriu.sGSkills.org.skills.abilities.Ability;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.utils.MathUtils;

public class MageExplosionSpell extends Ability {
    public MageExplosionSpell() {
        super("Mage", "explosion_spell");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMageAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ConfigurationSection chances = getOptions(info, "hoe-chance").getSection();
        XMaterial material = XMaterial.matchXMaterial(player.getInventory().getItemInMainHand());
        String equation = chances.getString(material.name());
        if (equation == null) return;
        int hoeChance = (int) getAbsoluteScaling(info, equation, "damage", event.getDamage());

        if (MathUtils.hasChance(hoeChance)) {
            Entity entity = event.getEntity();
            int lvl = info.getAbilityLevel(this);
            double damage = this.getScaling(info, "damage", event);

            if (lvl > 2 && event.getEntity() instanceof Player && MathUtils.hasChance(1, 1_000)) {
                MessageHandler.sendPlayerMessage(player, "&4Ｃrimson-&0black &6blaze&5, king of myriad worlds, though I promulgate the laws of nature, " +
                        "I am the alias of destruction incarnate in accordance with the principles of all creation.");
                MessageHandler.sendPlayerMessage((Player) entity, "&cYou've been struck by an unexpected crismon detonation power.");
                entity.setMetadata("UNEXP-BIO", new FixedMetadataValue(SkillsPro.get(), null));
                event.setDamage(damage * damage);

                Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                    if (!event.getEntity().isDead()) event.getEntity().removeMetadata("UNEXP-BIO", SkillsPro.get());
                }, 1L);

                XSound.ENTITY_GENERIC_EXPLODE.play(entity);
                if (XMaterial.supports(12)) XSound.UI_TOAST_CHALLENGE_COMPLETE.play(entity);
                Particles.meguminExplosion(SkillsPro.get(), 3, ParticleDisplay.of(XParticle.FLAME).withLocation(entity.getLocation()));
                player.getWorld().spawnParticle(XParticle.EXPLOSION_EMITTER.get(), entity.getLocation(), lvl * 3, 1, 1, 1);
                return;
            }

            player.getWorld().spawnParticle(XParticle.EXPLOSION_EMITTER.get(), entity.getLocation(), lvl * 2, 1, 1, 1);
            XSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.play(entity);
            event.setDamage(event.getDamage() + damage);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().hasMetadata("UNEXP-BIO")) {
            event.setDeathMessage("&6" + event.getEntity().getName() + " &4has been struck by &6" +
                    event.getEntity().getKiller().getName() + "'s &4unexpected crismon detonation power.");
            event.getEntity().removeMetadata("UNEXP-BIO", SkillsPro.get());
        }
    }
}
