package uk.antiperson.stackmob.listeners.entity;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.entity.StackTools;
import uk.antiperson.stackmob.tools.GlobalValues;

public class InteractEvent implements Listener {

    private StackMob sm;
    public InteractEvent(StackMob sm) {
        this.sm = sm;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if(!(StackTools.hasValidData(entity))){
            return;
        }
        if(StackTools.hasValidMetadata(entity, GlobalValues.BREED_MODE) &&
                entity.getMetadata(GlobalValues.BREED_MODE).get(0).asBoolean()){
            return;
        }
        if(event.getHand() == EquipmentSlot.OFF_HAND){
            return;
        }
        if(event.isCancelled()){
            return;
        }

        int stackSize = StackTools.getSize(entity);
        if(entity instanceof Animals){
            if(correctFood(event.getPlayer().getInventory().getItemInMainHand(), entity) && ((Animals) entity).canBreed()){
                if(StackTools.hasSizeMoreThanOne(entity)) {
                    if (sm.getCustomConfig().getBoolean("multiply.breed")) {
                        int breedSize = stackSize;
                        int handSize = event.getPlayer().getInventory().getItemInMainHand().getAmount();
                        if (handSize < breedSize) {
                            breedSize = event.getPlayer().getInventory().getItemInMainHand().getAmount();
                            event.getPlayer().getInventory().setItemInMainHand(null);
                        }

                        int childAmount = breedSize / 2;
                        Animals child = (Animals) sm.getTools().duplicate(entity);
                        StackTools.setSize(child, childAmount);
                        child.setBaby();

                        event.getPlayer().getInventory().getItemInMainHand().setAmount(handSize - breedSize);
                        ((Animals) entity).setBreed(false);
                    } else if (sm.getCustomConfig().getBoolean("divide-on.breed")) {
                        Entity newEntity = sm.getTools().duplicate(entity);
                        StackTools.setSize(newEntity,stackSize - 1);

                        StackTools.makeSingle(entity);
                        entity.setMetadata(GlobalValues.NO_STACK, new FixedMetadataValue(sm, true));
                        entity.setMetadata(GlobalValues.BREED_MODE, new FixedMetadataValue(sm, true));

                        // Allow to stack after breeding
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!entity.isDead()) {
                                    entity.setMetadata(GlobalValues.BREED_MODE, new FixedMetadataValue(sm, false));
                                    entity.setMetadata(GlobalValues.NO_STACK, new FixedMetadataValue(sm, false));
                                }
                            }
                        }.runTaskLater(sm, 20 * 20);
                    }
                    return;
                }
            }
        }
        if(sm.getCustomConfig().getBoolean("divide-on.name")) {
            ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
            if (handItem.getType() == Material.NAME_TAG && handItem.getItemMeta().hasDisplayName()) {
                if (stackSize > 1) {
                    Entity dupe = sm.getTools().duplicate(entity);
                    StackTools.setSize(dupe,stackSize - 1);
                }
                StackTools.removeSize(entity);
            }
        }
    }

    // There should be a method in bukkit for this...
    private boolean correctFood(ItemStack is, Entity entity) {
        Material type = is.getType();
        switch (entity.getType()) {
            case COW:
            case SHEEP:
                return type == Material.WHEAT;
            case PIG:
                return (type == Material.CARROT || type == Material.BEETROOT || type == Material.POTATO);
            case CHICKEN:
                return type == Material.WHEAT_SEEDS
                        || type == Material.MELON_SEEDS
                        || type == Material.BEETROOT_SEEDS
                        || type == Material.PUMPKIN_SEEDS;
            case HORSE:
                return (type == Material.GOLDEN_APPLE || type == Material.GOLDEN_CARROT) && ((Horse)entity).isTamed();
            case WOLF:
                return (type == Material.BEEF
                        || type == Material.CHICKEN
                        || type == Material.COD
                        || type == Material.MUTTON
                        || type == Material.PORKCHOP
                        || type == Material.RABBIT
                        || type == Material.SALMON
                        || type == Material.COOKED_BEEF
                        || type == Material.COOKED_CHICKEN
                        || type == Material.COOKED_COD
                        || type == Material.COOKED_MUTTON
                        || type == Material.COOKED_PORKCHOP
                        || type == Material.COOKED_RABBIT
                        || type == Material.COOKED_SALMON)
                        && ((Wolf) entity).isTamed();
            case OCELOT:
                return (type == Material.SALMON
                        || type == Material.COD
                        || type == Material.PUFFERFISH
                        || type == Material.TROPICAL_FISH)
                        && ((Ocelot) entity).isTamed();
            case RABBIT:
                return type == Material.CARROT || type == Material.GOLDEN_CARROT || type == Material.DANDELION;
            case LLAMA:
                return type == Material.HAY_BLOCK;
            case TURTLE:
                return type == Material.SEAGRASS;
        }
        return false;
    }
}
