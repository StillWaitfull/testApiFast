import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import suites.RegressStage;

class Runner {
    public static void main(String[] args) {
        JUnitCore.runClasses(ParallelComputer.methods(), RegressStage.class);
    }
}
