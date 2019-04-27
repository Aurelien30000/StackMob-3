package uk.antiperson.stackmob.entity.multiplication;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.loot.LootContext;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.compat.PluginCompat;
import uk.antiperson.stackmob.compat.hooks.CustomDropsHook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to calculate the correct amount of entity drops.
 */
public class DropTools {

    private StackMob sm;
    public DropTools(StackMob sm){
        this.sm = sm;
    }

    public void doDrops(int deadAmount, LivingEntity dead){
        if(deadAmount > sm.getCustomConfig().getInt("multiply-drops.entity-limit")){
            deadAmount = sm.getCustomConfig().getInt("multiply-drops.entity-limit");
        }
        Map<ItemStack, Integer> rawDrops = calculateDrops(deadAmount, dead);
        if(sm.getCustomConfig().getBoolean("multiply-drops.compress")){
            rawDrops = compressDrops(rawDrops);
        }
        for(Map.Entry<ItemStack, Integer> entry : rawDrops.entrySet()){
            Collection<ItemStack> stacks = convert(entry.getKey(), entry.getValue());
            dropStacks(stacks, dead.getLocation());
        }
    }

    private Map<ItemStack, Integer> calculateDrops(int deadAmount, LivingEntity dead){
        Map<ItemStack, Integer> drops = new HashMap<>();
        for(int i = 0; i < deadAmount; i++){
            for(ItemStack stack : generateLoot(dead)){
                if(stack == null || stack.getType() == Material.AIR){
                    continue;
                }
                if(sm.getCustomConfig().getStringList("multiply-drops.drops-blacklist")
                        .contains(stack.getType().toString())){
                    continue;
                }
                if(sm.getCustomConfig().getStringList("multiply-drops.drop-one-per")
                        .contains(stack.getType().toString())){
                    stack.setAmount(1);
                }
                for(ItemStack itemStack : drops.keySet()){
                    if(itemStack.isSimilar(stack)){
                        drops.put(itemStack, drops.get(itemStack) + stack.getAmount());
                        break;
                    }
                }
                if(!drops.containsKey(stack)) {
                    drops.put(stack, stack.getAmount());
                }
            }
        }
        return drops;
    }

    private Collection<ItemStack> generateLoot(LivingEntity dead){
        if(sm.getHookManager().isHookRegistered(PluginCompat.CUSTOMDROPS)){
            CustomDropsHook cdh = (CustomDropsHook) sm.getHookManager().getHook(PluginCompat.CUSTOMDROPS);
            if(cdh.hasCustomDrops(dead)){
                return cdh.getDrops(dead);
            }
        }
        LootContext lootContext = new LootContext.Builder(dead.getLocation()).lootedEntity(dead).killer(dead.getKiller()).build();
        return ((Mob) dead).getLootTable().populateLoot(ThreadLocalRandom.current(), lootContext);
    }

    // Calculate a random drop amount.
    public int calculateAmount(int multiplier){
        return (int) Math.round((0.75 + ThreadLocalRandom.current().nextDouble(2)) * multiplier);
    }

    public void dropEggs(ItemStack drop, int amount, Location dropLocation){
        Collection<ItemStack> drops = convert(drop, amount);
        drops.forEach(itemStack -> itemStack.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1));
        dropStacks(drops, dropLocation);
    }

    public void dropDrops(ItemStack drop, int amount, Location dropLocation){
        Collection<ItemStack> drops = convert(drop, amount);
        dropStacks(drops, dropLocation);
    }

    private void dropStacks(Collection<ItemStack> drops, Location location){
        drops.forEach(itemStack -> location.getWorld().dropItemNaturally(location, itemStack));
    }

    // Method to create itemstacks.
    private Collection<ItemStack> convert(ItemStack drop, int amount){
        List<ItemStack> items = new ArrayList<>();
        double inStacks = (double) amount / (double) drop.getMaxStackSize();
        double floor = Math.floor(inStacks);
        double leftOver = inStacks - floor;
        for(int i = 0; i < floor; i++){
            ItemStack newStack = drop.clone();
            newStack.setAmount(drop.getMaxStackSize());
            items.add(newStack);
        }
        if(leftOver > 0){
            ItemStack newStack = drop.clone();
            newStack.setAmount((int) Math.round(leftOver * drop.getMaxStackSize()));
            items.add(newStack);
        }
        return items;
    }

    private boolean dropIsArmor(LivingEntity entity, ItemStack drop){
        if(entity.getEquipment().getItemInMainHand().equals(drop) || entity.getEquipment().getItemInOffHand().equals(drop)){
            return true;
        }
        for(ItemStack itemStack : entity.getEquipment().getArmorContents()){
            if(itemStack.equals(drop)){
                return true;
            }
        }
        return false;
    }

    private Map<ItemStack, Integer> compressDrops(Map<ItemStack, Integer> items){
        Map<ItemStack, Integer> list = new HashMap<>();
        for(Map.Entry<ItemStack, Integer> entry : items.entrySet()){
            ItemStack item = entry.getKey();
            int amount = entry.getValue();
            Iterator<Recipe> recipes = sm.getServer().recipeIterator();
            while (recipes.hasNext()){
                Recipe recipe = recipes.next();
                if(recipe instanceof ShapedRecipe){
                    ShapedRecipe slRecipe = (ShapedRecipe) recipe;
                    if(slRecipe.getIngredientMap().values().size() < 9){
                        continue;
                    }
                    if(slRecipe.getIngredientMap().values().stream()
                            .anyMatch(itemStack -> notValid(item, itemStack))){
                        continue;
                    }
                    double totalAmount =  (double) amount / 9D;
                    int blockAmount = (int) Math.floor(totalAmount);
                    int leftOver = (int) Math.round((totalAmount - blockAmount) * 9);
                    list.put(recipe.getResult(), blockAmount);
                    list.put(item, leftOver);
                }
            }
            if(!list.containsKey(item)){
                list.put(item, amount);
            }
        }
        return list;
    }

    private boolean notValid(ItemStack original, ItemStack recipeItem){
        if(recipeItem == null){
            return true;
        }
        return original.getType() != recipeItem.getType();
    }
}
