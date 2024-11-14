package works.nuka.modularkit;

import works.nuka.modularkit.events.ModuleStatus;
import works.nuka.modularkit.ex.*;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})

public class ModuleManager {
    private final ModularSource modSource;
    private final Map<String, ArrayList<ModularModule>> modulesDependencies = new HashMap<>();

    /**
     * The ModuleManager - Manage your Modules !
     *
     * @param source - Give a ModularSource which contains all modules.
     * @throws ModSourceEx - Cause a ModSourceEx if the source is null.
     * @since 1.0
     */

    public ModuleManager(ModularSource source) throws ModSourceEx {
        if (source != null)
            modSource = source;
        else
            throw new ModSourceEx("a ModularSource cannot be null.");
    }

    /**
     * Run the Module
     *
     * @param module - Give the Module needed to run.
     * @return Return true if the runModule operation is successful.
     * @throws ModRegisterEx - Return a ModRegisterEx if Module Registration fails.
     * @since 1.0
     */

    public synchronized boolean runModule(ModularModule module, Runnable onComplete) throws ModRegisterEx {
        HashMap<String, ModularModule> runMap = (HashMap<String, ModularModule>) modSource.getModuleMap();
        if (!runMap.isEmpty()) {
            if (runMap.containsKey(module.getUuid())) {
                Thread runThread = getRunThread(module);
                // Starting the module...
                module.setModuleStatus(ModuleStatus.RUNNING); // Force RUNNING status
                runThread.start();
                if (onComplete != null) {
                    onComplete.run();
                }
                return true;
            } else
                throw new ModRegisterEx("the module is not registered !");
        } else
            throw new ModRegisterEx("Module not found :/");
    }

    private Thread getRunThread(ModularModule module) {
        Thread runThread = new Thread(() -> {
            try {
                module.exec();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                // Stop the module
                try {
                    this.stopModule(module, false, null);
                    module.setModuleStatus(ModuleStatus.STOPPED);
                } catch (ModRunEx e) {
                    throw new RuntimeException(e);
                }
            }
        });

        runThread.setName("Mod_" + module.getModuleName() + "_" + module.getUuid());
        return runThread;
    }

    public void runModule(String uuid, Runnable onComplete) throws ModRunEx {
        try {
            ModularModule mod = findModuleByUuiD(uuid);
            if (mod == null)
                throw new ModRunEx("Module not found !");

            runModule(mod, onComplete);

        } catch (ModUuidEx | ModRegisterEx e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the Module
     *
     * @param module    - Give the Module Object to stop.
     * @param forceStop - Force Stop a Module.
     * @throws ModRunEx - Return a ModRunEx if an error occur.
     * @since 1.0
     */

    public void stopModule(ModularModule module, @Deprecated boolean forceStop, Runnable onComplete) throws ModRunEx {
        if (module.getModuleStatus() == ModuleStatus.RUNNING) {
            module.setModuleStatus(ModuleStatus.STOPPING);
            module.stop();
            if (forceStop)
                module.kill();

            module.setModuleStatus(ModuleStatus.STOPPED);
        }

        if (onComplete != null) {
            onComplete.run();
        }
    }

    public void stopModule(String uuid, @Deprecated boolean forceStop, Runnable onComplete) throws ModRunEx {
        try {
            ModularModule mod = findModuleByUuiD(uuid);
            if (mod == null)
                throw new ModRunEx("Module not found !");

            stopModule(mod, forceStop, onComplete);
        } catch (ModUuidEx e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds and Return a ModularModule Object by UuID
     *
     * @param uuid - Give the needed Module-Uuid.
     * @return - Return the found ModularModule.
     * @throws ModUuidEx - Return a ModUuidEx if the Module-UuID is incorrect.
     * @since 1.0
     */

    public ModularModule findModuleByUuiD(String uuid) throws ModUuidEx {
        if (uuid.length() == 8) {
            if (modSource.getModuleMap().containsKey(uuid))
                return modSource.getModuleMap().get(uuid);
        } else
            throw new ModUuidEx("The uuid is incorrect !");
        return null;
    }

    /**
     * Set module dependencies
     *
     * @param modDeps - Give an array of ModularModule Objects.
     * @since 1.3
     */

    public void setDepends(ModularModule module, ModularModule... modDeps) throws ModSourceEx {
        if (modulesDependencies.containsKey(module.getUuid())) {
            throw new ModSourceEx("Cant update setDepends for... TODO");
        } else {
            modulesDependencies.put(module.getUuid(), new ArrayList<>(List.of(modDeps)));
        }
    }
    
    public ArrayList<ModularModule> getDepends(ModularModule module) throws ModSourceEx {
        ArrayList<ModularModule> modules = modulesDependencies.get(module.getUuid());
        if (modules != null) return modules;
        else throw new ModSourceEx("Module not found");
    }
}
