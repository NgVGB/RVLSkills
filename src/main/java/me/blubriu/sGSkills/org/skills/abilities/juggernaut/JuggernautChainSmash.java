package me.blubriu.sGSkills.org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import me.blubriu.sGSkills.org.skills.abilities.AbilityContext;
import me.blubriu.sGSkills.org.skills.abilities.InstantActiveAbility;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.main.locale.SkillsLang;
import me.blubriu.sGSkills.org.skills.managers.DamageManager;
import me.blubriu.sGSkills.org.skills.utils.EntityUtil;
import me.blubriu.sGSkills.org.skills.utils.MathUtils;

public class JuggernautChainSmash extends InstantActiveAbility {
    private static final String CHAIN_SMASH = "CHAIN_SMASH";

    public JuggernautChainSmash() {
        super("Juggernaut", "chain_smash");
    }

    @SuppressWarnings("SameParameterValue")
    private static void explosionWave(JavaPlugin plugin, double rate, ParticleDisplay display, ParticleDisplay secDisplay) {
        new BukkitRunnable() {
            final double addition = Math.PI * 0.1;
            final double rateDiv = Math.PI / rate;
            double times = Math.PI / 4;

            public void run() {
                times += addition;
                for (double theta = 0; theta <= Math.PI * 2; theta += rateDiv) {
                    double x = times * Math.cos(theta);
                    double y = 2 * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    double z = times * Math.sin(theta);
                    display.spawn(x, y, z);

                    theta = theta + Math.PI / 64;
                    x = times * Math.cos(theta);
                    // y = 2 * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    z = times * Math.sin(theta);
                    secDisplay.spawn(x, y, z);
                }

                if (times > 20) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void tntDmaage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof TNTPrimed)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Entity tnt = event.getDamager();
        if (!tnt.hasMetadata(CHAIN_SMASH)) return;

        Player player = (Player) tnt.getMetadata(CHAIN_SMASH).get(0).value();
        if (player == event.getEntity()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!SkilledPlayer.getSkilledPlayer((OfflinePlayer) event.getEntity()).getActiveAbilities().remove(this))
            return;
        event.setCancelled(true);
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        info.setActiveAbilitiy(this, true);
        double initialLaunch = getScaling(info, "initial-launch");
        player.setVelocity(new Vector(0, initialLaunch, 0));
        XSound.ENTITY_HORSE_JUMP.play(player, 3, 0);
        ParticleDisplay cloud = ParticleDisplay.of(XParticle.CLOUD).withLocation(player.getLocation()).withCount(100).offset(1);
        cloud.spawn();

        double damage = getScaling(info, "damage");
        double range = getScaling(info, "range");
        double launch = getScaling(info, "explosion-launch");
        double smashForce = getScaling(info, "smash-force");

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            player.setVelocity(new Vector(0, smashForce, 0));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isFlying()) return;
                    Location loc = player.getLocation();

                    // If they're not on the ground yet.
                    if (!MathUtils.isInteger(loc.getY())) return;

                    cancel();
                    cloud.spawn(loc);

                    if (!getOptions(info, "disable-explosion").getBoolean()) {
                        TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(loc, XEntityType.TNT.get());
                        tnt.setMetadata(CHAIN_SMASH, new FixedMetadataValue(SkillsPro.get(), player));
                        tnt.setFuseTicks(0);
                    }

                    XSound.ENTITY_GENERIC_EXPLODE.play(player, (float) range, XSound.DEFAULT_PITCH);
                    ParticleDisplay display = ParticleDisplay.of(XParticle.EXPLOSION_EMITTER.get()).withCount(10).offset(1);
                    explosionWave(SkillsPro.get(), 20, ParticleDisplay.of(XParticle.FIREWORK).withLocation(loc),
                            ParticleDisplay.of(XParticle.WITCH).withLocation(loc));

                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (EntityUtil.filterEntity(player, entity)) continue;

                        DamageManager.damage((LivingEntity) entity, player, damage);
                        entity.setVelocity(new Vector(0, launch, 0));
                        display.spawn(entity.getLocation());
                    }

                    info.setActiveAbilitiy(JuggernautChainSmash.this, false);
                }
            }.runTaskTimer(SkillsPro.get(), 5L, 1L);
        }, 20L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTp(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (info.getLastAbilityUsed() != this) return;
        SkillsLang.COMMAND_BINDINGS_CHANGED.sendMessage(player);
    }
}
