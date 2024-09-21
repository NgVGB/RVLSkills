package me.blubriu.sGSkills.org.skills.data.database.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.blubriu.sGSkills.org.skills.data.managers.Cosmetic;
import me.blubriu.sGSkills.org.skills.data.managers.CosmeticCategory;
import me.blubriu.sGSkills.org.skills.data.managers.PlayerSkill;
import me.blubriu.sGSkills.org.skills.data.managers.SkilledPlayer;
import me.blubriu.sGSkills.org.skills.events.SkillsPersonalBonus;
import me.blubriu.sGSkills.org.skills.utils.FastUUID;

import java.lang.reflect.Type;
import java.util.*;

public class AdapterSkilledPlayer implements JsonSerializer<SkilledPlayer>, JsonDeserializer<SkilledPlayer> {
    @Override
    public SkilledPlayer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        SkilledPlayer info = new SkilledPlayer();
        JsonObject json = jsonElement.getAsJsonObject();

        Type mapType = new TypeToken<Map<String, PlayerSkill>>() {}.getType();
        info.setSkills(context.deserialize(json.get("skills"), mapType));
        PlayerSkill activeSkill = info.getSkills().get(json.get("skill").getAsString());
        if (activeSkill == null) activeSkill = new PlayerSkill(PlayerSkill.NONE);
        AdapterSharedSkillsData.deserialize(jsonElement, activeSkill, true);
        info.setAbsoluteActiveSkill(activeSkill);

        JsonElement showAbEle = json.get("showActionBar");
        if (showAbEle != null) info.setShowActionBar(showAbEle.getAsBoolean());

        JsonElement lastChange = json.get("lastSkillChange");
        info.setLastSkillChange(lastChange == null ? 0 : lastChange.getAsLong());

        Type uuidType = new TypeToken<Set<UUID>>() {}.getType();
        Set<UUID> friends = context.deserialize(json.get("friends"), uuidType);
        Set<UUID> friendRequests = context.deserialize(json.get("friendRequests"), uuidType);
        if (friends != null) info.setFriends(friends);
        if (friendRequests != null) info.setFriendRequests(friendRequests);

        JsonElement element = json.get("healthScaling");
        if (element != null) info.setHealthScaling(element.getAsDouble());

        mapType = new TypeToken<HashMap<String, Integer>>() {}.getType();
        info.setMasteries(context.deserialize(json.get("masteries"), mapType));

        mapType = new TypeToken<List<SkillsPersonalBonus>>() {}.getType();
        try {
            List<SkillsPersonalBonus> bonuses = context.deserialize(json.get("bonuses"), mapType);
            info.setBonuses(bonuses);
        } catch (Exception ignored) {
            // ignored.printStackTrace();
            // Don't save old bonuses
        }

        JsonElement cosmeticsElement = json.get("cosmetics");
        if (cosmeticsElement != null) {
            mapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> cosmetics = context.deserialize(cosmeticsElement, mapType);
            Map<String, Cosmetic> realCosmetics = new HashMap<>();
            for (Map.Entry<String, String> cosmetic : cosmetics.entrySet()) {
                CosmeticCategory cat = CosmeticCategory.get(cosmetic.getKey());
                if (cat != null) realCosmetics.put(cat.getName(), cat.getCosmetic(cosmetic.getValue()));
            }
            info.setCosmetics(realCosmetics);
        }

        return info;
    }

    @Override
    public JsonElement serialize(SkilledPlayer info, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        AdapterSharedSkillsData.serialize(json, info.getActiveSkill(), true);
        json.addProperty("skill", info.getSkillName());
        json.addProperty("showActionBar", info.showActionBar());
        json.addProperty("lastSkillChange", info.getLastSkillChange());

        Type uuidType = new TypeToken<Set<UUID>>() {}.getType();
        json.addProperty("healthScaling", info.getHealthScaling());
        json.add("friends", context.serialize(info.getFriends(), uuidType));
        json.add("friendRequests", context.serialize(info.getFriendRequests(), uuidType));

        JsonObject skills = new JsonObject();
        for (Map.Entry<String, PlayerSkill> skill : info.getSkills().entrySet()) {
            // if (skill.getKey().equals(PlayerSkill.NONE)) continue;
            skills.add(skill.getKey(), context.serialize(skill.getValue()));
        }
        json.add("skills", skills);

        Type mapType = new TypeToken<HashMap<String, Integer>>() {}.getType();
        json.add("masteries", context.serialize(info.getMasteries(), mapType));

        mapType = new TypeToken<Collection<SkillsPersonalBonus>>() {}.getType();
        json.add("bonuses", context.serialize(info.getBonuses(), mapType));

        JsonObject cosmetics = new JsonObject();
        for (Map.Entry<String, Cosmetic> cosmetic : info.getCosmetics().entrySet()) {
            cosmetics.addProperty(cosmetic.getKey(), cosmetic.getValue().getName());
        }
        json.add("cosmetics", cosmetics);

        return json;
    }
}