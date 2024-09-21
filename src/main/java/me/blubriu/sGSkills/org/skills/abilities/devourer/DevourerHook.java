package me.blubriu.sGSkills.org.skills.abilities.devourer;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.base.Enums;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.blubriu.sGSkills.org.skills.abilities.AbilityContext;
import me.blubriu.sGSkills.org.skills.abilities.InstantActiveAbility;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.utils.Cooldown;
import me.blubriu.sGSkills.org.skills.utils.LocationUtils;
import me.blubriu.sGSkills.org.skills.utils.MathUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DevourerHook extends InstantActiveAbility {
    private static final String COOLDOWN = "DEVOURER_HOOK";
    private static final Map<Integer, Player> HOOKS = new HashMap<>();
    private static final Set<Integer> HOOKED = new HashSet<>();

    static {
        addDisposableHandler(HOOKED);
        addDisposableHandler(HOOKS);
    }

    public DevourerHook() {
        super("Devourer", "hook");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        Entity arrow = player.getWorld().spawnEntity(player.getEyeLocation(), Enums.getIfPresent(EntityType.class, getOptions(info, "hook").getString()).orNull());
        arrow.setVelocity(player.getLocation().getDirection().multiply(info.getAbilityLevel(this) + 1));
        HOOKS.put(arrow.getEntityId(), player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                HOOKED.remove(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void hookDamage(EntityDamageByEntityEvent event) {
        if (HOOKED.remove(event.getDamager().getEntityId())) event.setCancelled(true);
    }

    @EventHandler
    public void onSprint(PlayerMoveEvent event) {
        if (MathUtils.isInteger(event.getTo().getY())) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(SkillsPro.get(), () -> {
                if (Cooldown.isInCooldown(event.getPlayer().getUniqueId(), COOLDOWN)) return;
                HOOKED.remove(event.getPlayer().getEntityId());
            }, 2L);
        }
    }

    @EventHandler
    public void onHooked(ProjectileHitEvent event) {
        Entity arrow = event.getEntity();
        Player player = HOOKS.remove(arrow.getEntityId());
        if (player == null) return;
        if (event.getHitEntity() != null) HOOKED.add(arrow.getEntityId());

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        Location location = player.getLocation();
        Location arrowLoc = arrow.getLocation();
        if (location.distance(arrowLoc) > getScaling(info, "range")) return;

        Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            XMaterial mat = XMaterial.matchXMaterial(hitBlock.getType());
            List<String> untargetable = getOptions(info, "untargetable-blocks").getStringList();
            if (untargetable.contains(mat.name())) return;
        }

        LocationUtils.whoooosh(player, arrowLoc, 0.15, 0.05, 0.15);
        ParticleDisplay.of(XParticle.CLOUD).withLocation(location).withCount(50).offset(1, 0, 1).spawn();
        try {
            XSound.ENTITY_FISHING_BOBBER_RETRIEVE.play(player);
        } catch (IllegalArgumentException ignored) {
        }

        if (info.getAbilityLevel(this) > 1) {
            HOOKED.add(player.getEntityId());
            new Cooldown(player.getUniqueId(), COOLDOWN, 2, TimeUnit.SECONDS);
        }
        new BukkitRunnable() {
            final ParticleDisplay dis = ParticleDisplay.of(XParticle.LARGE_SMOKE).withLocation(null);
            int times = 0;

            @Override
            public void run() {
                times++;
                dis.spawn(player.getLocation());
                if (times > 20) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);
    }
}
