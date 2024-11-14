package works.nuka.modularkit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import works.nuka.modularkit.events.ModuleStatus;
import works.nuka.modularkit.ex.*;

import static org.junit.jupiter.api.Assertions.*;

public class SourceTests {

    private static final String TEST_UUID = "09040865";

    @BeforeEach
    void setUp() {
        // Clear the static map before tests
        ModularSource.getSourceMap().clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        ModularSource.getSourceMap().clear();
    }

    @Test
    void testConstructorWithNullUuid() throws ModSourceEx {
        assertThrows(ModUuidEx.class, () -> new ModularSource(null));
    }

    @Test
    void testConstructorWithIncorrectUuid() {
        assertThrows(ModUuidEx.class, () -> new ModularSource("tooLongUuid"));
    }

    @Test
    void testConstructorWithValidUuid() throws ModUuidEx, ModSourceEx {
        ModularSource source = new ModularSource(TEST_UUID);
        assertNotNull(source);
        assertEquals(TEST_UUID, source.getUuid());
        assertTrue(ModularSource.getSourceMap().containsKey(TEST_UUID));
    }

    @Test
    void testFindSourceByUuiD() throws ModUuidEx, ModSourceEx {
        ModularSource source = new ModularSource(TEST_UUID);
        assertEquals(source, ModularSource.findSourceByUuiD(TEST_UUID));
        assertThrows(ModUuidEx.class, () -> ModularSource.findSourceByUuiD("invalidUUID")); // Invalid UUID
        assertNull(ModularSource.findSourceByUuiD("notfound")); // UUID 8 chars
    }

    @Test
    void testRegisterAndUnregisterSource() throws ModUuidEx, ModSourceEx {
        ModularSource source = new ModularSource(TEST_UUID);
        assertTrue(source.destroy(false));
        assertFalse(ModularSource.getSourceMap().containsKey(TEST_UUID));
    }

    @Test
    void testDestroyForceTrue() throws ModUuidEx, ModSourceEx {
        ModularSource source = new ModularSource(TEST_UUID);
        assertTrue(source.destroy(true));
    }

    @Test
    void testRegisterModule() throws Exception {
        ModularSource source = new ModularSource(TEST_UUID);
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        assertTrue(source.getModuleMap().containsKey(module.getUuid()));
    }

    @Test
    void testRegisterModuleAlreadyInstantiated() throws Exception {
        ModularSource source = new ModularSource(TEST_UUID);
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        assertThrows(ModRegisterEx.class, () -> source.registerModule(module));
    }

    @Test
    void testUnregisterModule() throws Exception {
        ModularSource source = new ModularSource(TEST_UUID);
        ModularModule module = new ModuleTest();
        source.registerModule(module);
        assertTrue(source.unregisterModule(module));
        assertFalse(source.getModuleMap().containsKey(module.getUuid()));
    }

    @Test
    void testUnregisterRunningModule() throws Exception {
        ModularSource source = new ModularSource(TEST_UUID);
        ModularModule module = new ModuleTest();
        module.setModuleStatus(ModuleStatus.RUNNING); // Simulate module running
        source.registerModule(module);
        assertThrows(ModRegisterEx.class, () -> source.unregisterModule(module));
    }
}