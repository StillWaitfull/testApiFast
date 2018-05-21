package suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import tests.climate.ClimateTests;
import toolkit.ParallelClassAndMethodsSuite;

@RunWith(ParallelClassAndMethodsSuite.class)
@Suite.SuiteClasses({
        ClimateTests.class
})
public class RegressStage {

}
