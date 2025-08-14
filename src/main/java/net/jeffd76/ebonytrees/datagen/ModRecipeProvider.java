package net.jeffd76.ebonytrees.datagen;

import net.jeffd76.ebonytrees.block.ModBlocks;
import net.jeffd76.ebonytrees.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {

        //Shaped Recipe Example
        //ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SAPPHIRE_BLOCK.get())
        //        .pattern("SSS")
        //        .pattern("SSS")
        //        .pattern("SSS")
        //        .define('S', ModItems.SAPPHIRE.get())
        //        .unlockedBy(getHasName(ModItems.SAPPHIRE.get()), has(ModItems.SAPPHIRE.get()))
        //        .save(pWriter);

        //Sapeless Reciipe Example
        //ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SAPPHIRE.get(), 9)
        //        .requires(ModBlocks.SAPPHIRE_BLOCK.get())
        //        .unlockedBy(getHasName(ModBlocks.SAPPHIRE_BLOCK.get()))
        //        .save(pWriter);
    }
}
