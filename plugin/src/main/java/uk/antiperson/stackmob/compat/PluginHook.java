package uk.antiperson.stackmob.compat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import uk.antiperson.stackmob.StackMob;

public abstract class PluginHook implements PluginChecks{

    private StackMob stackMob;
    private HookManager hookManager;
    private Plugin plugin;
    private String pluginName;
    private PluginCompat pluginCompat;
    public PluginHook(HookManager hm, StackMob sm, PluginCompat hooks){
        plugin = Bukkit.getPluginManager().getPlugin(hooks.getName());
        pluginName = hooks.getName();
        stackMob = sm;
        pluginCompat = hooks;
        hookManager = hm;
    }

    public String getPluginName() {
        return pluginName;
    }

    public Plugin getPlugin(){
        return plugin;
    }

    public StackMob getStackMob() {
        return stackMob;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public PluginCompat getPluginCompat() {
        return pluginCompat;
    }

}
