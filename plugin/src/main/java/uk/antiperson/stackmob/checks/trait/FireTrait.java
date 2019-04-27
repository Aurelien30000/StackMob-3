package uk.antiperson.stackmob.checks.trait;

import org.bukkit.entity.Entity;
import uk.antiperson.stackmob.checks.ApplicableTrait;
import uk.antiperson.stackmob.checks.TraitManager;

public class FireTrait implements ApplicableTrait {

    private TraitManager traitManager;
    public FireTrait(TraitManager traitManager){
        this.traitManager = traitManager;
        if(traitManager.getStackMob().getCustomConfig().getBoolean("fire-ticks.enabled")) {
            traitManager.registerTrait(this);
        }
    }

    @Override
    public boolean checkTrait(Entity original, Entity nearby) {
        return false;
    }

    @Override
    public void applyTrait(Entity original, Entity spawned) {
        if(traitManager.getStackMob().getCustomConfig().getStringList("fire-ticks.blacklist")
                .contains(original.getType().toString())){
            return;
        }
        spawned.setFireTicks(original.getFireTicks());
    }
}
