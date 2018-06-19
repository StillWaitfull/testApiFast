package toolkit;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelClassAndMethodsSuite extends Suite {

    private static final int numberOfThreads = (System.getenv("numberOfThreads") == null ? 8 : Integer.parseInt(System.getenv("numberOfThreads")));
    private final ExecutorService fService = Executors.newFixedThreadPool(numberOfThreads);

    private final RunnerScheduler schedulerForClasses = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            fService.submit(childStatement);
        }

        public void finished() {
            try {
                fService.shutdown();
                fService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    };


    public ParallelClassAndMethodsSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        setScheduler(schedulerForClasses);

    }
}
