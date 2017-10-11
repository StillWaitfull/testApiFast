package suites;

import com.googlecode.junittoolbox.ParallelSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import tests.climate.ClimateTests;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
        ClimateTests.class
})
public class RegressStage {

}
