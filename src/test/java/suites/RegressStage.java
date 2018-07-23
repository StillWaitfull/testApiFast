package suites;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;
import tests.climate.ClimateTests;

@RunWith(JUnitPlatform.class)
@SuiteDisplayName("DEMO")
@SelectClasses({ClimateTests.class})
public class RegressStage {

}
