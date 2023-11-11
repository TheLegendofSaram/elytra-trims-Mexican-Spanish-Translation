package dev.kikugie.elytratrims.recipe;

import dev.kikugie.elytratrims.ElytraTrimsServer;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;

public class ElytraPatternRecipe extends SpecialCraftingRecipe {
    //#if MC > 12001
    public ElytraPatternRecipe(CraftingRecipeCategory category) {
        super(category);
    }
    //#else
    //$$ public ElytraPatternRecipe(net.minecraft.util.Identifier id, CraftingRecipeCategory category) {
    //$$     super(id, category);
    //$$ }
    //#endif

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int elytra = 0;
        int banner = 0;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() instanceof ElytraItem) {
                elytra++;
            } else if (stack.getItem() instanceof BannerItem) {
                if (BannerBlockEntity.getPatternCount(stack) == 0) return false;
                banner++;
            } else if (!stack.isEmpty()) {
                return false;
            }

            if (elytra > 1 || banner > 1) return false;
        }

        return elytra == 1 && banner == 1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack elytra = ItemStack.EMPTY;
        ItemStack banner = ItemStack.EMPTY;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof ElytraItem) {
                elytra = stack.copy();
            } else if (stack.getItem() instanceof BannerItem) {
                banner = stack;
            }
        }

        NbtList patterns = BannerBlockEntity.getPatternListNbt(banner);
        if (patterns != null) {
            DyeColor color = ((BannerItem) banner.getItem()).getColor();
            NbtCompound compound = elytra.getOrCreateSubNbt("BlockEntityTag");
            compound.put("Patterns", patterns);
            compound.put("Base", NbtInt.of(color.getId()));
            ElytraTrimsServer.DYEABLE.setColor(elytra, color.getFireworkColor());
        } else if (elytra.getSubNbt("BlockEntityTag") != null) {
            elytra.removeSubNbt("BlockEntityTag");
            ElytraTrimsServer.DYEABLE.setColor(elytra, 0);
        }
        return elytra;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ElytraTrimsServer.ELYTRA_PATTERNS_RECIPE;
    }
}
