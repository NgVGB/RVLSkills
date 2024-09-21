package me.blubriu.sGSkills.org.skills.services.mobs;

import com.cryptomorin.xseries.reflection.XReflection;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import me.blubriu.sGSkills.org.skills.main.SLogger;
import me.blubriu.sGSkills.org.skills.utils.Pair;

public class ServiceMythicMobs {
    private static final boolean SUPPORTED = XReflection.supports(16);

    static {
        if (!SUPPORTED) SLogger.warn("MythicMobs support cannot be enabled for your server version.");
    }

    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        if (!SUPPORTED) return null;

        ActiveMob mob = MythicBukkit.inst().getMobManager().getMythicMobInstance(BukkitAdapter.adapt(entity));
        return mob == null ? null : Pair.of(mob.getType().getInternalName(), mob.getLevel());
    }
}
