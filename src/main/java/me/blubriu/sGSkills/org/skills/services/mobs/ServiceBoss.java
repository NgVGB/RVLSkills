package me.blubriu.sGSkills.org.skills.services.mobs;

import org.bukkit.entity.LivingEntity;
import me.blubriu.sGSkills.org.skills.utils.Pair;

/**
 * they don't give access to the jar anymore
 * https://github.com/kangarko/Boss/wiki/Developer-API#importing-the-api
 */
@Deprecated
public class ServiceBoss {
    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
//        Boss boss = BossAPI.getBoss(entity);
//        return boss == null ? null : Pair.of(boss.getName(), 0);
        return null;
    }
}
