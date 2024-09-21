package me.blubriu.sGSkills.org.skills.abilities;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.XTag;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import me.blubriu.sGSkills.org.skills.api.events.CustomHudChangeEvent;
import me.blubriu.sGSkills.org.skills.data.managers.PlayerAbilityData;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.main.SLogger;
import me.blubriu.sGSkills.org.skills.main.SkillsConfig;
import me.blubriu.sGSkills.org.skills.main.SkillsPro;
import me.blubriu.sGSkills.org.skills.main.locale.MessageHandler;
import me.blubriu.sGSkills.org.skills.managers.SkillItemManager;
import me.blubriu.sGSkills.org.skills.managers.resurrect.LastBreath;
import me.blubriu.sGSkills.org.skills.services.manager.ServiceHandler;
import me.blubriu.sGSkills.org.skills.types.SkillScaling;
import me.blubriu.sGSkills.org.skills.utils.LocationUtils;
import me.blubriu.sGSkills.org.skills.utils.nbt.ItemNBT;
import me.blubriu.sGSkills.org.skills.utils.nbt.NBTType;
import me.blubriu.sGSkills.org.skills.utils.nbt.NBTWrappers;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AbilityListener implements Listener {
    protected static final Cache<UUID, List<KeyBinding>> ACTIVATIONS = CacheBuilder.newBuilder()
            .expireAfterAccess(SkillsConfig.SKILL_ACTIVATION_TIME.getInt(), TimeUnit.MILLISECONDS).build();

    private static boolean keyBindingMatches(KeyBinding[] original, List<KeyBinding> current) {
        // Arrays.equals is redundant
        if (original.length < current.size()) return false;
        for (int i = 0; i < current.size(); i++) {
            if (original[i] != current.get(i)) return false;
        }
        return true;
    }

    private static boolean activate(Player player, KeyBinding action) {
        if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission("skills.use-creative")) return false;
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return false;
        if (SkillsConfig.LAST_BREATH_ENABLED.getBoolean() && LastBreath.isLastBreaths(player)) return false;

        SLogger debugging = new SLogger();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasSkill()) {
            debugging.add("Player has no skill").show();
            return false;
        }
        if (info.isActiveReady()) {
            debugging.add("Player has no active ready abilities.").show();
            return false;
        }

        ActiveAbility ability = null;
        List<KeyBinding> keys = ACTIVATIONS.getIfPresent(player.getUniqueId());
        if (keys == null) {
            // Sneaking at the beginning doesn't work for abilities that need it internally.
            if (action == KeyBinding.SNEAK) return false;
            ACTIVATIONS.put(player.getUniqueId(), keys = new ArrayList<>(4));
        }

        // if (action != KeyBinding.SNEAK && player.isSneaking()) keys.add(KeyBinding.WHILE_SNEAK);
        keys.add(action);
        debugging.add("Current key combinations: " + keys);

        ItemStack item = player.getItemInHand();
        for (Ability abs : info.getSkill().getAbilities()) {
            if (abs.isPassive()) continue;

            ActiveAbility activeAb = (ActiveAbility) abs;
            PlayerAbilityData data = info.getAbilityData(activeAb);

            if (data.isDisabled()) continue;
            if (data.getLevel() <= 0) continue;
            if (!activeAb.isWeaponAllowed(info, item)) continue;
            KeyBinding[] activation = data.getKeyBinding() != null ?
                    data.getKeyBinding() : activeAb.getActivationKey(info);

            if (!keyBindingMatches(activation, keys)) continue;

            // That's the ability we're looking for.
            if (keys.size() == activation.length) {
                ability = activeAb;
                break;
            }

            debugging.add("No skill was found with the given key binding combinations.").show();
            return true;
        }

        ACTIVATIONS.invalidate(player.getUniqueId());
        if (ability == null) {
            debugging.add("No skill was found with the given key binding combinations.").show();
            return false;
        }

        debugging.add("Ability: " + ability.name);
        if (ability.isPvPBased() && SkillsConfig.DISABLE_ABILITIES_IN_REGIONS.getBoolean() && ServiceHandler.isPvPOff(player)) {
            debugging.add("Ability cannot be used in disabled worldguard regions.").show();
            return false;
        }

        // Cooldown
        if (info.isInCooldown()) {
            debugging.add("Ability is in cooldown.").show();
            XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
            return true;
        }

        // Energy
        double energy = ability.getEnergy(info);
        if (info.getEnergy() < energy) {
            debugging.add("Not enough energy for ability: " + info.getEnergy() + " < " + energy).show();
            XSound.play(info.getSkill().getEnergy().getSoundNotEnough(), x -> x.forPlayers(player));
            return true;
        }

        String ready = ability.getAbilityReady(info);
        if (info.showReadyMessage() && ready != null) ability.sendMessage(player, ready);
        if (!info.setActiveReady(ability, true)) {
            debugging.add("Ability is not actively ready.").show();
            return true;
        }
        CustomHudChangeEvent.call(player);
        info.setLastAbilityUsed(ability);

        // Activate instantly if that's what the active requires
        if (ability instanceof InstantActiveAbility) {
            if (ability.checkup(player) != null) {
                InstantActiveAbility instantActiveAbility = (InstantActiveAbility) ability;
                AbilityContext context = new AbilityContext(player, info, instantActiveAbility);
                instantActiveAbility.useSkill(context);
            }
            debugging.add("Ability has been instantly activated.").show();
            return true;
        }


        ParticleDisplay display = ParticleDisplay
                .of(XParticle.of(SkillsConfig.READY_PARTICLE_PARTICLE.getString()).orElse(XParticle.DUST))
                .withLocation(player.getLocation());
        display.count = (int) SkillsConfig.READY_PARTICLE_COUNT.eval(info, ability);
        double offset = SkillsConfig.READY_PARTICLE_OFFSET.eval(info, ability);
        display.offset(offset, offset, offset);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                display.spawn(LocationUtils.getHandLocation(player, true));
                if (!info.isActiveReady()) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);

        // Un-ready the active if the player doesn't do anything with it
        XSound.ITEM_ARMOR_EQUIP_DIAMOND.play(player);
        ActiveAbility finalAb = ability;
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            task.cancel();
            if (!player.isOnline()) return;
            if (info.setActiveReady(finalAb, false)) {
                String idle = finalAb.getAbilityIdle(info);
                if (info.showReadyMessage() && idle != null) player.sendMessage(idle);
                XSound.ITEM_ARMOR_EQUIP_CHAIN.play(player);
            }
        }, ability.getIdle(info) * 20L);
        debugging.add("Ability requires activation by hitting a target.").show();
        return true;
    }

    @EventHandler
    public void onSneakActivate(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) activate(player, KeyBinding.SNEAK);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordMove(InventoryPickupItemEvent event) {
        if (Ability.isSkillEntity(event.getItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onQ(PlayerDropItemEvent event) {
        if (activate(event.getPlayer(), KeyBinding.DROP)) event.setCancelled(true);
    }

    @EventHandler
    public void onF(PlayerSwapHandItemsEvent event) {
        if (activate(event.getPlayer(), KeyBinding.SWITCH)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDisposablePlayerHandler(PlayerQuitEvent event) {
        int id = event.getPlayer().getEntityId();

        disposeTasks(event.getPlayer());
        for (Set<Integer> set : Ability.DISPOSABLE_ENTITIES_SET) set.remove(id);
        for (Map<Integer, ?> map : Ability.DISPOSABLE_ENTITIES_MAP) map.remove(id);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        disposeTasks(event.getEntity());
    }

    static void disposeTasks(Player player) {
        List<BukkitTask> disposableTasks = Ability.DISPOSABLE_TASKS.remove(player.getEntityId());
        if (disposableTasks != null) disposableTasks.forEach(BukkitTask::cancel);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSkillDamageCap(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        double cap = info.getScaling(SkillScaling.DAMAGE_CAP);
        if (cap == 0) return;

        if (event.getFinalDamage() > cap) event.setDamage(cap);
    }

    @EventHandler
    public void onSkillActivate(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Action action = event.getAction();
        if (action == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null) {
            try {
                NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
                nbt.get(SkillItemManager.SKILL_ITEM, NBTType.STRING);
            } catch (Exception ex) {
                MessageHandler.sendConsolePluginMessage("&cA NBT error has occurred! Please report this to the developer.");
                throw new RuntimeException(ex);
            }

            Material type = item.getType();
            // Don't activate abilities if the player is trying to eat.
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                if (type.isEdible()) return;

            // Check if your item in hand is blacklisted
            XMaterial mat = XMaterial.matchXMaterial(item);
            if (XTag.anyMatchString(mat, SkillsConfig.PREVENT_ACTIVATION_ITEMS.getStringList())) return;

            // If you're trying to place/break blocks, ignore.
            if (action == Action.RIGHT_CLICK_BLOCK) {
                Block clicked = event.getClickedBlock();
                if (item.getType().isBlock()) return;
                if (XTag.anyMatchString(XMaterial.matchXMaterial(clicked.getType()), SkillsConfig.PREVENT_ACTIVATION_BLOCKS.getStringList()))
                    return;
            }

            KeyBinding activationAction = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK ?
                    KeyBinding.LEFT_CLICK :
                    KeyBinding.RIGHT_CLICK;
            activate(player, activationAction);
        }
    }
}
