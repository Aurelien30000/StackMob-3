package uk.antiperson.stackmob.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.tasks.StackTask;

import java.util.List;

public class ServerLoad  implements Listener {

    private StackMob sm;
    public ServerLoad(StackMob sm){
        this.sm = sm;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event){
        List<World> worlds = Bukkit.getWorlds();
        for(int i = 0; i < worlds.size(); i++){
            int period = (int) Math.round(sm.getCustomConfig().getDouble("task-delay") / worlds.size()) * i;
            new StackTask(sm, worlds.get(i)).runTaskTimer(sm, period, 100);
        }
    }
}
