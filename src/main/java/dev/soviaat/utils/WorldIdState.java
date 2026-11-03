package dev.soviaat.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.UUID;

public class WorldIdState extends SavedData {
    private String worldUuid;

    public WorldIdState() {
        this.worldUuid = UUID.randomUUID().toString();
    }

    public WorldIdState(String worldUuid) {
        this.worldUuid = worldUuid;
    }

    public static final Codec<WorldIdState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("WorldUUID").forGetter(WorldIdState::getWorldUuid)
            ).apply(instance, WorldIdState::new)
    );

    public static final SavedDataType<WorldIdState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("statify", "world_id"),
            WorldIdState::new,
            CODEC,
            null
    );

    public String getWorldUuid() {
        return this.worldUuid;
    }

    public static String getWorldUuid(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        WorldIdState state = overworld.getDataStorage().computeIfAbsent(WorldIdState.TYPE);
        state.setDirty();
        return state.getWorldUuid();
    }
}