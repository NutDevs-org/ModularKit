package works.nuka.modularkit;

import works.nuka.modularkit.events.ModuleStatus;
import works.nuka.modularkit.ex.ModRegisterEx;
import works.nuka.modularkit.ex.ModSourceEx;
import works.nuka.modularkit.ex.ModUuidEx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("unused")
public class ModularSource {

    private static final HashMap<String, ModularSource> sourceMap = new HashMap<>();
    private final HashMap<String, ModularModule> moduleMap = new HashMap<>();
    private final String uuid;
    private ModuleManager moduleManager;

    /**
     * ModularSource - Create a collections of Modules.
     *
     * @param _uuid - UuID of the ModularSource.
     * @throws ModUuidEx Returns a ModUuidEx if the UuiD is incorrect or null.
     */

    public ModularSource(String _uuid) throws ModUuidEx {
        try {
            this.moduleManager = new ModuleManager(this);
        } catch (ModSourceEx modSourceEx) {
            modSourceEx.printStackTrace();
        }

        if (_uuid == null)
            throw new ModUuidEx("uuid cannot be null.");

        if (_uuid.length() != 8)
            throw new ModUuidEx("uuid is incorrect !");

        uuid = _uuid;
        registerSource();
    }

    /**
     * ModularSource - Create a collections of Modules with External ModularModule
     * Repository.
     *
     * @param _uuid - UuID of the ModularSource.
     * @param path  - File Path of the Modules Repository.
     * @throws ModUuidEx - Can return a ModUuidEx if UuID is incorrect or null.
     */
    public ModularSource(String _uuid, File path, String fileExtension) throws ModUuidEx, ModSourceEx {
        if (_uuid == null)
            throw new ModUuidEx("uuid cannot be null.");

        if (_uuid.length() != 8)
            try {
                throw new ModUuidEx("uuid is incorrect !"); // Grab only the group 1 of the uuid eg :
                // "**f3eafee8**-2419-4a58-b8fe-fe72d6f4019b"
            } catch (Exception e) {
                e.printStackTrace();
            }

        if (fileExtension.isEmpty())
            throw new ModSourceEx("You need to set a custom file extension !");

        try {
            this.moduleManager = new ModuleManager(this);
        } catch (ModSourceEx modSourceEx) {
            modSourceEx.printStackTrace();
        }

        uuid = _uuid;

        if (path.exists() && path.canRead()) {
            try {
                Files.walk(path.toPath()).forEach(e -> {
                    if (e.toFile().isFile()) {
                        URLClassLoader classLoader = null;

                        try {
                            classLoader = new URLClassLoader(
                                    new URL[]{new URL("file", null, e.toFile().getAbsolutePath())});
                        } catch (MalformedURLException malformedURLException) {
                            malformedURLException.printStackTrace();
                        }

                        Properties extModRegistrationFile = new Properties();

                        try {
                            assert classLoader != null; // Safe check if classLoader is null.
                            InputStream inModlr = classLoader.getResourceAsStream(".modlr");
                            if (inModlr != null) {
                                extModRegistrationFile.load(inModlr);
                            } else
                                throw new IOException("File .modlr not found !");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                        for (Map.Entry<Object, Object> entry : extModRegistrationFile.entrySet()) {
                            Class<?> modClass = null;

                            try {
                                modClass = Class.forName(entry.getValue().toString(), false, classLoader);

                            } catch (Exception classNotFoundException) {
                                classNotFoundException.printStackTrace();
                            }

                            assert modClass != null;

                            if (modClass.getSuperclass().getName().equals(ModularModule.class.getName())) {
                                ModularModule newModule = null;
                                try {
                                    newModule = (ModularModule) modClass.getDeclaredConstructor().newInstance();
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                                         | NoSuchMethodException ex) {
                                    ex.printStackTrace();
                                }
                                try {
                                    assert newModule != null;
                                    registerModule(newModule);
                                } catch (ModRegisterEx | ModUuidEx | ModSourceEx ex) {
                                    ex.printStackTrace();
                                }
                            } else
                                try {
                                    throw new ModSourceEx("The module doesn't extends to ModularModule.");
                                } catch (ModSourceEx ex) {
                                    ex.printStackTrace();
                                }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Finally, register the source !
        registerSource();
    }

    /**
     * Find a ModularSource with the source-uuid.
     *
     * @param uuid - Give the Source-UuID.
     * @return - Returns a found ModularSource Object.
     * @throws ModUuidEx - Can returns a ModUuidEx if the uuid is incorrect or null.
     */
    public static ModularSource findSourceByUuiD(String uuid) throws ModUuidEx {
        if (uuid.length() == 8) {
            if (sourceMap.containsKey(uuid))
                return sourceMap.get(uuid);
        } else
            throw new ModUuidEx("uuid is incorrect");
        return null;
    }

    /**
     * (Protected, Internal API) - get the ModularSource sourceMap.
     *
     * @return - Returns a ModularSource HashMap Object.
     */
    protected static HashMap<String, ModularSource> getSourceMap() {
        return sourceMap;
    }

    /**
     * Get a ModularSource unmodifiable-hashmap
     *
     * @return - Return a ModularSource Unmodifioble HashMap.
     */
    public static HashMap<String, ModularSource> getUnmodifiableSourceMap() {
        return (HashMap<String, ModularSource>) Collections.unmodifiableMap(sourceMap);
    }

    private synchronized boolean registerSource() {
        if (!sourceMap.containsKey(uuid)) {
            sourceMap.put(uuid, this);
            return true;
        }
        return false;
    }

    private boolean unregisterSource() {
        if (sourceMap.containsKey(uuid)) {
            sourceMap.remove(uuid, this);
            return true;
        }
        return false;
    }

    /**
     * Destroys the entire source
     *
     * @param forceDestroy - Force the Destruction.
     * @return - Return true if destroy success.
     * @since 1.0
     */
    public boolean destroy(@Deprecated boolean forceDestroy) {
        for (Map.Entry<String, ModularModule> moduleEntry : moduleMap.entrySet()) {
            moduleEntry.getValue().setModuleStatus(ModuleStatus.STOPPING);
            moduleEntry.getValue().stop();

            if (forceDestroy) {
                try {
                    moduleEntry.getValue().kill(); // Force stop the module
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return unregisterSource();
    }

    /**
     * Registering a module with the ModularModule Object.
     *
     * @param module - Module Object
     * @return - Return true if the module registration will success.
     * @throws ModRegisterEx - Can returns a ModRegisterEx if the Module
     *                       registration will fail.
     * @throws ModUuidEx     - If the uuid is incorrect or uuid is null.
     * @throws ModSourceEx   - Can fail if the ModSource is null.
     * @since 1.0
     */
    public boolean registerModule(ModularModule module) throws ModRegisterEx, ModUuidEx, ModSourceEx {
        if (!moduleMap.containsKey(module.getUuid())) {
            module.setModuleSource(this);
            moduleMap.put(module.getUuid(), module);

        } else
            throw new ModRegisterEx("Module already instantiated !");

        return true;
    }

    /**
     * Registering a module with the ModuleClass.
     *
     * @param module Class Object contains a ModularModule Object.
     * @return - Return true if the module registration will success.
     * @throws ModUuidEx              - Can fail if null of incorrect.
     * @throws ModRegisterEx          - Can fail if null of incorrect.
     * @throws ModSourceEx            - Can fail if null of incorrect.
     * @throws InstantiationException - Can return an error if the module is already
     *                                instanced.
     * @throws IllegalAccessException - Can return a Ex if Illegal Access violation
     *                                subsist.
     * @since 1.0
     */
    public boolean registerModule(Class<?> module)
            throws ModUuidEx, ModRegisterEx, ModSourceEx, InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ModularModule mod = (ModularModule) module.getDeclaredConstructor().newInstance();
        return registerModule(mod);
    }

    /**
     * Unregister a Module quickly
     *
     * @param module - Give the module to unregister.
     * @return Returns true if the operation success.
     * @throws ModRegisterEx - Can fail if the module is null.
     */
    public boolean unregisterModule(ModularModule module) throws ModRegisterEx {
        System.out.println(module.getModuleStatus());
        if (moduleMap.containsKey(module.getUuid())) {
            if (module.getModuleStatus() == ModuleStatus.RUNNING)
                throw new ModRegisterEx("Failed to unregister the module : the module is running.");
            else {
                moduleMap.remove(module.getUuid(), module);
                return true;
            }
        }

        return false;
    }

    public Map<String, ModularModule> getUnmodifiableModuleMap() {
        return Collections.unmodifiableMap(moduleMap);
    }

    protected Map<String, ModularModule> getModuleMap() {
        return moduleMap;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public String getUuid() {
        return uuid;
    }
}
