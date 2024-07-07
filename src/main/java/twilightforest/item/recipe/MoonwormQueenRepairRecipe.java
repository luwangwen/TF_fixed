package twilightforest.item.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;

import java.util.ArrayList;
import java.util.List;

public class MoonwormQueenRepairRecipe extends CustomRecipe {

	public MoonwormQueenRepairRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		ItemStack queen = null;
		List<ItemStack> berries = new ArrayList<>();

		for (int i = 0; i < input.size(); ++i) {
			ItemStack stackInQuestion = input.getItem(i);
			if (!stackInQuestion.isEmpty()) {
				if (stackInQuestion.is(TFItems.MOONWORM_QUEEN.get()) && stackInQuestion.isDamaged()) {
					queen = stackInQuestion;
				}
				if (stackInQuestion.is(TFItems.TORCHBERRIES.get())) {
					berries.add(stackInQuestion);
				}
			}
		}
		return queen != null && !berries.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider access) {
		List<Item> berries = new ArrayList<>();
		ItemStack queen = null;
		for (int i = 0; i < input.size(); ++i) {
			ItemStack itemstack = input.getItem(i);
			if (!itemstack.isEmpty()) {
				if (itemstack.is(TFItems.MOONWORM_QUEEN.get())) {
					if (queen == null) {
						queen = itemstack;
					} else {
						//Only accept 1 queen
						return ItemStack.EMPTY;
					}
				}

				if (itemstack.is(TFItems.TORCHBERRIES.get())) {
					//add all berries in the grid to a list to determine the amount to repair
					berries.add(itemstack.getItem());
				}
			}
		}

		if (!berries.isEmpty() && queen != null && queen.isDamaged()) {
			ItemStack newQueen = TFItems.MOONWORM_QUEEN.get().getDefaultInstance();
			//each berry repairs 64 durability
			newQueen.setDamageValue(queen.getDamageValue() - (berries.size() * 64));
			return newQueen;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.MOONWORM_QUEEN_REPAIR_RECIPE.get();
	}
}
