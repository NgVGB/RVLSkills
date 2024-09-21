package me.blubriu.sGSkills.org.skills.utils.versionsupport;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import me.blubriu.sGSkills.org.skills.utils.MathUtils;

import java.awt.*;

public class VersionSupport {
    public static ExperienceOrb dropExp(Location loc, int amount) {
        ExperienceOrb orb = loc.getWorld().spawn(loc, ExperienceOrb.class);
        orb.setExperience(amount);
        return orb;
    }

    public void ensurePatch() {
        int latestPatch = XReflection.getLatestPatchNumberOf(XReflection.MINOR_NUMBER);
        if (latestPatch < XReflection.PATCH_NUMBER) {
            throw new IllegalStateException("Your server is currently running a patch version that's not supported: "
                    + XReflection.getVersionInformation() + " Update to the latest patch: v1." + XReflection.MINOR_NUMBER + '.' + latestPatch);
        }
    }

    public static int getHealthPercent(LivingEntity entity) {
        return getHealthPercent(entity, 0);
    }

    public static int getHealthPercent(LivingEntity entity, EntityDamageEvent event) {
        return getHealthPercent(entity, event.getFinalDamage());
    }

    public static int getHealthPercent(LivingEntity entity, double offset) {
        return (int) MathUtils.getPercent(entity.getHealth() - offset, VersionSupport.getMaxHealth(entity));
    }

    public static void heal(LivingEntity entity, double amount) {
        EntityRegainHealthEvent regain = new EntityRegainHealthEvent(entity, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(regain);
        amount = Math.min(entity.getHealth() + regain.getAmount(), VersionSupport.getMaxHealth(entity));
        entity.setHealth(amount);
    }

    public static void spawnColouredDust(Location loc) {
        spawnColouredDust(loc, Color.black);
    }

    public static void spawnColouredDust(Location loc, java.awt.Color color) {
        if (XMaterial.supports(13)) {
            VersionSupportFuture.spawnColouredDust(loc, color);
        } else {
            VersionSupportOld.spawnColouredDust(loc, color.getRGB());
        }
    }

    public static boolean isPassenger(Entity e, Entity pass) {
        if (XMaterial.supports(13)) {
            return VersionSupportFuture.isPassenger(e, pass);
        } else {
            return VersionSupportOld.isPassenger(e, pass);
        }
    }

    public static double getMaxHealth(LivingEntity e) {
        if (XMaterial.supports(13)) {
            return VersionSupportFuture.getMaxHealth(e);
        } else {
            return VersionSupportOld.getMaxHealth(e);
        }
    }

    public static void addHealth(LivingEntity entity, double amount) {
        if (amount == 0) return;
        if (XMaterial.supports(13)) VersionSupportFuture.setMaxHealth(entity, entity.getHealth() + amount);
        else VersionSupportOld.setMaxHealth(entity, entity.getHealth() + amount);
    }

    public static void setMaxHealth(LivingEntity entity, double amount) {
        if (amount == 0) return; // Disables a feature
        if (amount < 0) throw new IllegalArgumentException("Invalid max health for player: " + amount);
        if (XMaterial.supports(13)) VersionSupportFuture.setMaxHealth(entity, amount);
        else VersionSupportOld.setMaxHealth(entity, amount);
    }

    public static boolean isCropFullyGrown(Block crop) {
        if (XMaterial.supports(13)) {
            return VersionSupportFuture.isCropFullyGrown(crop);
        } else {
            return VersionSupportOld.isCropFullyGrown(crop);
        }
    }
}
