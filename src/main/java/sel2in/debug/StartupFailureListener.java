package sel2in.debug;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

public class StartupFailureListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        System.err.println("=========-------============================");
        System.err.println("!!! ERROR STARTUP FAILURE !!!");
        System.err.println("=============---------======================");
        
        Throwable exception = event.getException();
        
        
        
        exception.printStackTrace(); 
        System.err.println("Reason: " + exception.getMessage());
    }
}
