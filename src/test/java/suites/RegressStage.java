package suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import tests.climate.ClimateTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ClimateTests.class

})
public class RegressStage {

}
