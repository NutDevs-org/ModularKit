package works.nuka.modularkit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import works.nuka.modularkit.events.ModuleStatus;
import works.nuka.modularkit.ex.*;

import static org.junit.jupiter.api.Assertions.*;

public class ModularModuleTests {

    private static final String TEST_UUID = "0123456a";
    private static final String TEST_NAME = "TestModule";
    private static final String TEST_AUTHOR = "TestAuthor";
    private static final String TEST_VERSION = "1.0";
    private ModularModule testModule;

    // A concrete subclass for testing purposes
    private static class ConcreteModularModule extends ModularModule {
        public ConcreteModularModule(String name, String uuid, String author, String version, ModularModule... modDeps) throws ModUuidEx {
            super(name, uuid, author, version, modDeps);
        }

        @Override
        protected void start() {
            System.out.printf("Hello %s!%n", this.getModuleName());
            System.out.println("Thread name: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void load() {

        }

        @Override
        protected void unload() {

        }

        @Override
        protected void stop() {
            System.out.printf("Stopping %s...%n", this.getModuleName());
        }
    }

    @BeforeEach
    void setUp() throws ModUuidEx {
        testModule = new ConcreteModularModule(TEST_NAME, TEST_UUID, TEST_AUTHOR, TEST_VERSION);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        ModularSource.getSourceMap().clear();
    }

    @Test
    void testConstructorWithValidParameters() {
        assertEquals(TEST_NAME, testModule.getModuleName(), "Module name should match");
        assertEquals(TEST_UUID, testModule.getUuid(), "Module UUID should match");
        assertEquals(TEST_AUTHOR, testModule.getAuthor(), "Module author should match");
        assertEquals(TEST_VERSION, testModule.getVersion(), "Module version should match");
        assertEquals(ModuleStatus.STOPPED, testModule.getModuleStatus(), "Module should start stopped");
    }

    @Test
    void testConstructorWithNullUuid() {
        assertThrows(ModUuidEx.class, () -> new ConcreteModularModule(TEST_NAME, null, TEST_AUTHOR, TEST_VERSION));
    }

    @Test
    void testConstructorWithInvalidUuidLength() {
        assertThrows(ModUuidEx.class, () -> new ConcreteModularModule(TEST_NAME, "short", TEST_AUTHOR, TEST_VERSION));
    }

    @Test
    void testConstructorWithEmptyName() throws ModUuidEx {
        ModularModule emptyNameModule = new ConcreteModularModule("", TEST_UUID, TEST_AUTHOR, TEST_VERSION);
        assertEquals("I Have a no-name !", emptyNameModule.getModuleName(), "Should set a default name when empty");
    }

    @Test
    void testAddToModuleSource() throws ModSourceEx, ModUuidEx, ModRegisterEx {
        ModularSource source = new ModularSource(TEST_UUID);
        source.registerModule(testModule);
        assertTrue(source.getUnmodifiableModuleMap().containsKey(testModule.getUuid()), "Should contains the module in the source");
    }

    @Test
    void testSetModuleSourceWithNullSource() {
        assertThrows(ModSourceEx.class, () -> testModule.setModuleSource(null));
    }

    @Test
    void testSetModuleSourceWithExistingModule() throws ModRegisterEx, ModSourceEx, ModUuidEx {
        ModularSource source = new ModularSource(TEST_UUID);
        ModularModule existingModule = new ConcreteModularModule("Existing", TEST_UUID, "Someone", "1.0");
        source.registerModule(existingModule);

        // Expect this to throw because the UUID already exists in the source
        assertThrows(ModRegisterEx.class, () -> source.registerModule(existingModule));
    }

    @Test
    void testExec() throws ModUuidEx, ModRegisterEx, ModSourceEx {
        ModularSource source = new ModularSource(TEST_UUID);
        source.registerModule(testModule);
        source.getModuleManager().runModule(testModule, () -> {
            assertEquals(ModuleStatus.RUNNING, testModule.getModuleStatus(), "Module should be running after exec");
        });
    }

    @Test
    void testStop() throws ModUuidEx, ModRegisterEx, ModSourceEx, ModRunEx {
        ModularSource source = new ModularSource(TEST_UUID);
        source.registerModule(testModule);
        source.getModuleManager().runModule(testModule, () -> {
            assertEquals(ModuleStatus.RUNNING, testModule.getModuleStatus(), "Module should be running");

            // Now kill it.
            assertDoesNotThrow(() -> {
                source.getModuleManager().stopModule(testModule, false, () -> {
                    assertEquals(ModuleStatus.STOPPED, testModule.getModuleStatus(), "Module should be STOPPED");
                });
            }, "Should not throw an exception when module status is STOPPING");
        });
    }

    @Test
    void testKillWithWrongStatus() {
        assertThrows(ModRunEx.class, () -> testModule.kill(), "Should throw an exception when module status isn't STOPPING");
    }

    @Test
    void testSetModuleStatus() {
        testModule.setModuleStatus(ModuleStatus.RUNNING);
        assertEquals(ModuleStatus.RUNNING, testModule.getModuleStatus());
    }

    @Test
    void testDependencies() throws ModUuidEx, ModRegisterEx, ModSourceEx {
        ModularModule depMod = new ConcreteModularModule("DepModule", "0123456a", TEST_AUTHOR, TEST_VERSION);
        testModule = new ConcreteModularModule(TEST_NAME, TEST_UUID, TEST_AUTHOR, TEST_VERSION, depMod);

        ModularSource source = new ModularSource(TEST_UUID);
        source.registerModule(testModule);
        assertNotNull(source.getUnmodifiableModuleMap().get("0123456a"));
    }
}
