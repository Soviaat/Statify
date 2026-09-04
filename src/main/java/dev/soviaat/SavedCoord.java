package dev.soviaat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SavedCoord {
    private String name;
    private int x;
    private int y;
    private int z;
    private String dimension;
    private String itemId;

    public SavedCoord (String name, int x, int y, int z, String dimension, String itemId) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension != null ? dimension.toLowerCase() : "overworld";
        this.itemId = itemId != null ? itemId : "minecraft:compass";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }

    public String getCoordsText() { return x + " " + y + " " + z; }

    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension.toLowerCase(); }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public ItemStack getItemStack() {
        try {
            Identifier id = Identifier.tryParse(itemId);
            if (id != null) {
                Item item = BuiltInRegistries.ITEM.getValue(id);
                if (item != null && item != Items.AIR) return new ItemStack(item);
            }
        } catch (Exception _) {}
        return new ItemStack(Items.COMPASS);
    }

    public void setItemStack(ItemStack itemStack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (id != null) this.itemId = id.toString();
    }
    public String getFormattedDimensionName() {
        return switch (dimension.toLowerCase()) {
            case "nether", "the_nether" -> "§cNether";
            case "end", "the_end" -> "§5The End";
            default -> "§aOverworld";
        };
    }

}
