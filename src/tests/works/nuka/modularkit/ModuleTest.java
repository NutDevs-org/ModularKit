package works.nuka.modularkit;

public class ModuleTest extends ModularModule {

    public ModuleTest() throws Exception {
        super("ModuleTest", "81f9ab59", "Sundev79", "1.0.0", new AnotherModule());
    }

    @Override
    protected void stop() {
        System.out.println("Bye " + this.getModuleName() + " !");
    }

    @Override
    protected void start() {
        System.out.println("Hello " + this.getModuleName() + " !");
        try {
            Thread.sleep(5000);
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
}
