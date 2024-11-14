package works.nuka.modularkit;

import works.nuka.modularkit.events.ModuleStatus;
import works.nuka.modularkit.ex.ModRunEx;
import works.nuka.modularkit.ex.ModSourceEx;
import works.nuka.modularkit.ex.ModUuidEx;

@SuppressWarnings("unused")

public abstract class ModularModule {

    private final String uuid; // The Module UuID, needed for find a unique module.
    private final String moduleName; // module name.
    private final String author; // module author name.
    private final String version; // module version number.

    private ModuleStatus modStatus = ModuleStatus.STOPPED; // Default module execution status.
    private ModularSource modSource;
    private final ModularModule[] moduleDependencies;

    // Thread naming conventions : Mod_$name#$dynUuid_$uuid
    private String threadName;
    private Thread modThread;

    /**
     * The ModularModule Module Object, the fabulous "ModularModule" !
     *
     * @param _name   - Module Name.
     * @param _uuid   - Module UuID.
     * @param author  - Author of the Module.
     * @param version - Module Version Number.
     * @param modDeps - (Optional) Add Module Dependencies.
     * @throws ModUuidEx - Can return a ModUuidEx if the uuid is incorrect or null.
     * @since 1.0
     */

    public ModularModule(
            String _name,
            String _uuid,
            String author,
            String version,
            ModularModule... modDeps
    ) throws ModUuidEx {
        this.author = author;
        this.version = version;
        this.moduleDependencies = modDeps;

        if (_uuid == null)
            throw new ModUuidEx("uuid cannot be null.");

        else if (_uuid.length() != 8)
            throw new ModUuidEx("uuid is incorrect !");

        uuid = _uuid;

        if (_name.isEmpty())
            moduleName = "I Have a no-name !";

        else
            moduleName = _name;
    }

    protected void setModuleSource(ModularSource source) throws ModSourceEx, ModUuidEx {
        if (source != null)
            modSource = source;
        else
            throw new ModSourceEx("ModSource cannot be null !");

        if (modSource.getModuleManager().findModuleByUuiD(uuid) != null)
            throw new ModUuidEx("Module already instantiated !");

        if (this.moduleDependencies != null && this.moduleDependencies.length > 0)
            getModSource().getModuleManager().setDepends(this, this.moduleDependencies);
    }

    protected void exec() {
        modStatus = ModuleStatus.RUNNING;
        modThread = Thread.currentThread();
        threadName = modThread.getName();

        start();
    }

    /**
     * Stop the module
     */
    protected abstract void stop();

    protected abstract void start();

    protected abstract void load();

    protected abstract void unload();

    @SuppressWarnings("deprecation") // Because modThread.stop() is deprecated.
    protected void kill() throws ModRunEx {
        if (modStatus != ModuleStatus.STOPPING)
            throw new ModRunEx("Please try with stop() before call kill() !");
        modThread.stop();
    }

    public String getUuid() {
        return uuid;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public String getThreadName() {
        return threadName;
    }

    public ModuleStatus getModuleStatus() {
        return modStatus;
    }

    protected void setModuleStatus(ModuleStatus modStatus) {
        this.modStatus = modStatus;
    }

    private ModularSource getModSource() {
        return modSource;
    }
}
