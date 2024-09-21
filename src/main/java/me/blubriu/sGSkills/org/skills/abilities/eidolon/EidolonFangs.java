package me.blubriu.sGSkills.org.skills.abilities.eidolon;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import me.blubriu.sGSkills.org.skills.abilities.AbilityContext;
import me.blubriu.sGSkills.org.skills.abilities.InstantActiveAbility;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.managers.DamageManager;
import me.blubriu.sGSkills.org.skills.services.manager.ServiceHandler;

import java.util.Iterator;

public class EidolonFangs extends InstantActiveAbility {
    private static final String FANGS = "EIDOLON_FANGS";

    public EidolonFangs() {
        super("Eidolon", "fangs");
    }

    @Override
    public void useSkill(AbilityContext context) {
        if (!XMaterial.supports(11)) {
            MessageHandler.sendPlayerPluginMessage(context.getPlayer(), "&cCannot use this ability in this version of Minecraft.");
            return;
        }

        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();
        int amount = (int) getScaling(info, "fangs");

        EntityType type = XMaterial.supports(11) ? EntityType.EVOKER_FANGS : EntityType.FIREBALL;
        ParticleDisplay display = ParticleDisplay.of(XParticle.DRAGON_BREATH).withCount(20).offset(1);

        if (player.isSneaking()) {
            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof LivingEntity && entity.isValid() && entity.getType() != EntityType.ARMOR_STAND) {
                    Location loc = entity.getLocation();
                    Entity entityFang = player.getWorld().spawnEntity(loc, type);

                    entityFang.setMetadata(FANGS, new FixedMetadataValue(SkillsPro.get(), null));
                    if (type != EntityType.FIREBALL) {
                        EvokerFangs fangs = (EvokerFangs) entityFang;
                        fangs.setOwner(player);
                    }
                    display.spawn(loc);
                }
            }

            return;
        }

        Iterator<Block> blocks = new BlockIterator(player, amount);
        boolean isNew = XMaterial.supports(13);
        while (blocks.hasNext()) {
            Block block = blocks.next();
            Block corrected = null;
            if (block.getRelative(BlockFace.DOWN).getType().isSolid()) corrected = block;
            else {
                for (int i = 0; i > -5; i--) {
                    Block newBlock = block.getRelative(0, i, 0);
                    boolean isSolid = newBlock.getRelative(BlockFace.DOWN).getType().isSolid();
                    if (isNew) {
                        if (newBlock.isPassable() && isSolid) {
                            corrected = newBlock;
                            break;
                        }
                    } else {
                        if (!newBlock.getType().isSolid() && isSolid) {
                            corrected = newBlock;
                            break;
                        }
                    }
                    if (isSolid) break;
                }
                if (corrected == null) {
                    for (int i = 0; i < 5; i++) {
                        Block newBlock = block.getRelative(0, i, 0);
                        boolean isSolid = newBlock.getRelative(BlockFace.DOWN).getType().isSolid();

                        if (isNew) {
                            if (newBlock.isPassable() && isSolid) {
                                corrected = newBlock;
                                break;
                            }
                        } else {
                            if (!newBlock.getType().isSolid() && isSolid) {
                                corrected = newBlock;
                                break;
                            }
                        }
                    }
                }
            }
            if (corrected == null) corrected = player.getLocation().getBlock();
            Location loc = corrected.getLocation();

            Entity entity = player.getWorld().spawnEntity(loc, type);
            entity.setMetadata(FANGS, new FixedMetadataValue(SkillsPro.get(), null));
            if (type != EntityType.FIREBALL) {
                EvokerFangs fangs = (EvokerFangs) entity;
                fangs.setOwner(player);
            }
            display.spawn(loc);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFangsBite(EntityDamageByEntityEvent event) {
        if (!XMaterial.supports(11)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        Entity damager = event.getDamager();
        if (damager.getType() != EntityType.EVOKER_FANGS) return;
        if (!damager.hasMetadata(FANGS)) return;
        EvokerFangs fangs = (EvokerFangs) damager;

        event.setCancelled(true);
        if (!ServiceHandler.canFight(fangs.getOwner(), event.getEntity())) return;

        Player player = (Player) fangs.getOwner();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        ParticleDisplay display = ParticleDisplay.colored(damager.getLocation(), 255, 0, 0, 1);

        display.spawn();
        DamageManager.damage((LivingEntity) event.getEntity(), player, getScaling(info, "damage", event));
    }
}
