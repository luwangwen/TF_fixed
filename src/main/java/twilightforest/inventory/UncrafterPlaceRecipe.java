package twilightforest.inventory;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UncrafterPlaceRecipe<I extends RecipeInput, R extends Recipe<I>> extends ServerPlaceRecipe<I, R> implements UncraftingPlaceRecipe<Integer> {
	// Slots 0 & 1 are Uncrafting input & crafting output
	// Slots 2 to 10 are Uncrafting matrix
	// Slots 11 to 19 are Crafting matrix
	private static final int matrixOffset = 11;

	public UncrafterPlaceRecipe(RecipeBookMenu<I, R> menu) {
		super(menu);
	}

	@Override
	public void recipeClicked(ServerPlayer player, @Nullable RecipeHolder<R> recipe, boolean placeAll) {
		if (recipe != null && player.getRecipeBook().contains(recipe)) {
			this.inventory = player.getInventory();
			if (this.tryClearGrid() || player.isCreative()) {
				this.stackedContents.clear();
				player.getInventory().fillStackedContents(this.stackedContents);
				this.menu.fillCraftSlotsStackedContents(this.stackedContents);
				if (this.stackedContents.canCraft(recipe.value(), null)) {
					this.handleRecipeClicked(recipe, placeAll);
				} else {
					this.clearGrid();
					player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
				}

				player.getInventory().setChanged();
			}
		}
	}

	@Override
	protected void handleRecipeClicked(RecipeHolder<R> recipeHolder, boolean placeAll) {
		boolean flag = this.menu.recipeMatches(recipeHolder);
		int i = this.stackedContents.getBiggestCraftableStack(recipeHolder, null);
		if (flag) {
			for (int slot = 0; slot < this.menu.getSize(); ++slot) {
				if (slot != this.menu.getResultSlotIndex()) {
					ItemStack itemstack = this.menu.getSlot(slot).getItem();
					if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
						return;
					}
				}
			}
		}

		int j1 = this.getStackSize(placeAll, i, flag);
		IntList intlist = new IntArrayList();
		if (this.stackedContents.canCraft(recipeHolder.value(), intlist, j1)) {
			int k = j1;

			for (int l : intlist) {
				int i1 = StackedContents.fromStackingIndex(l).getMaxStackSize();
				if (i1 < k) {
					k = i1;
				}
			}

			if (this.stackedContents.canCraft(recipeHolder.value(), intlist, k)) {
				this.clearGrid();
				this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipeHolder, intlist.iterator(), k);
			}
		}
	}

	@Override
	protected int getStackSize(boolean placeAll, int maxPossible, boolean recipeMatches) {
		int i = 1;
		if (placeAll) {
			i = maxPossible;
		} else if (recipeMatches) {
			i = 64;

			for (int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight(); ++j) {
				ItemStack itemStack = this.menu.getSlot(j + matrixOffset).getItem();
				if (!itemStack.isEmpty() && i > itemStack.getCount()) {
					i = itemStack.getCount();
				}
			}

			if (i < 64) {
				++i;
			}
		}

		return i;
	}

	private int getAmountOfFreeSlotsInInventory() {
		int i = 0;

		for (ItemStack itemstack : this.inventory.items) {
			if (itemstack.isEmpty()) {
				++i;
			}
		}

		return i;
	}

	private boolean tryClearGrid() {
		List<ItemStack> list = Lists.newArrayList();
		int i = this.getAmountOfFreeSlotsInInventory();

		if (i > 0) {
			// Uncrafting input slot
			ItemStack itemstack = this.menu.getSlot(0).getItem().copy();
			if (!itemstack.isEmpty()) list.add(itemstack);
		}

		for (int slotIndex = 0; slotIndex < this.menu.getGridWidth() * this.menu.getGridHeight(); ++slotIndex) {
			ItemStack itemstack = this.menu.getSlot(slotIndex + matrixOffset).getItem().copy();
			if (!itemstack.isEmpty()) {
				int k = this.inventory.getSlotWithRemainingSpace(itemstack);
				if (k == -1 && list.size() <= i) {
					for (ItemStack itemstack1 : list) {
						if (ItemStack.isSameItem(itemstack1, itemstack)
							&& itemstack1.getCount() != itemstack1.getMaxStackSize()
							&& itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
							itemstack1.grow(itemstack.getCount());
							itemstack.setCount(0);
							break;
						}
					}

					if (!itemstack.isEmpty()) {
						if (list.size() >= i) {
							return false;
						}

						list.add(itemstack);
					}
				} else if (k == -1) {
					return false;
				}
			}
		}

		return true;
	}
}
