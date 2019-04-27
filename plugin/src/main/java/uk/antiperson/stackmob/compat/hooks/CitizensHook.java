package uk.antiperson.stackmob.compat.hooks;

import org.bukkit.entity.Entity;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.compat.HookManager;
import uk.antiperson.stackmob.compat.PluginCompat;
import uk.antiperson.stackmob.compat.PluginHook;
import uk.antiperson.stackmob.compat.Testable;

public class CitizensHook extends PluginHook implements Testable {

    public CitizensHook(HookManager hm, StackMob sm){
        super(hm, sm, PluginCompat.CITIZENS);
    }

    @Override
    public void enable(){
        if(getStackMob().getCustomConfig().getBoolean("check.is-citizens-npc")){
            getHookManager().registerHook(getPluginCompat(), this);
        }
    }

    @Override
    public boolean cantStack(Entity entity){
        return checkMetadata(entity);
    }

    private boolean checkMetadata(Entity entity){
        return entity.hasMetadata("NPC");
    }

}
