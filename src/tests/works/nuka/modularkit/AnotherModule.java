package works.nuka.modularkit;

public class AnotherModule extends ModularModule {

    public AnotherModule() throws Exception {
        super("AnotherModule", "452457c5", "Kawalize", "1.2.4");
    }

    @Override
    public void stop() {
        System.out.println("Bye " + this.getModuleName() + " !");
    }


    @Override
    public void start() {
        System.out.println("Hello " + this.getModuleName() + " !");
    }

    @Override
    protected void load() {

    }

    @Override
    protected void unload() {

    }
}
