package works.nuka.modularkit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import works.nuka.modularkit.events.ModuleStatus;
import works.nuka.modularkit.ex.*;

import static org.junit.jupiter.api.Assertions.*;

class ModuleManagerTest {

    private ModuleManager moduleManager;
    private ModularSource source;
    private static final String TEST_UUID = "09040865";

    @BeforeEach
    void setUp() throws ModSourceEx, ModUuidEx {
        source = new ModularSource(TEST_UUID); // Assuming ModularSource constructor with only UUID
        moduleManager = new ModuleManager(source);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        ModularSource.getSourceMap().clear();
        moduleManager = null;
        source = null;
    }

    @Test
    void testConstructorWithNullSource() {
        assertThrows(ModSourceEx.class, () -> new ModuleManager(null));
    }

    @Test
    void testRunModuleWithUnregisteredModule() throws Exception {
        ModularModule module = new ModuleTest();
        assertThrows(ModRegisterEx.class, () -> moduleManager.runModule(module, null));
    }

    @Test
    void testRunModule() throws Exception {
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        assertTrue(moduleManager.runModule(module, null));
        // Simulate the module running in a thread, for testing purposes we can check if the thread was named correctly
        assertTrue(Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.getName().equals("Mod_" + module.getModuleName() + "_" + module.getUuid())));
    }

    @Test
    void testStopModule() throws Exception {
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        moduleManager.runModule(module, () -> {
            assertEquals(ModuleStatus.RUNNING, module.getModuleStatus());
        });
        moduleManager.stopModule(module, false, () -> {
            assertEquals(ModuleStatus.STOPPED, module.getModuleStatus());
        }); // Stop it gracefully
    }

    // UuID 81f9ab59
    @Test
    void testStopModuleWithUuID() throws Exception {
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        moduleManager.runModule(module, () -> {
            assertEquals(ModuleStatus.RUNNING, module.getModuleStatus());
        }); // Start the module
        moduleManager.stopModule("81f9ab59", false, () -> {
            assertEquals(ModuleStatus.STOPPED, module.getModuleStatus());
        }); // Stop it gracefully
    }

    @Test
    void testFindModuleByUuiD() throws Exception {
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        assertEquals(module, moduleManager.findModuleByUuiD(module.getUuid()));
        assertThrows(ModUuidEx.class, () -> moduleManager.findModuleByUuiD("invalidUUID"));
    }

    @Test
    void testSetDepends() throws Exception {
        ModularModule module1 = new ModuleTest();
        ModularModule module2 = new AnotherModule();
        moduleManager.setDepends(module1, module2);
        assertTrue(moduleManager.getDepends(module1).contains(module2));
        // Test to break dependency, throw an error
        assertThrows(ModSourceEx.class, () -> moduleManager.getDepends(module2).contains(module1));
    }
}