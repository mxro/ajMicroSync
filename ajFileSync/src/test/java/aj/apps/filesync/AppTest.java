package aj.apps.filesync;

import aj.apps.filesync.internal.AjFileSyncData.WhenNewNodeCreated;
import aj.apps.filesync.internal.DataService;
import aj.apps.filesync.internal.engine.SyncEngine;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import one.client.jre.OneJre;
import one.common.One;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testSyncEngine() {
        OneJre.init();
        SyncEngine.processText("ignore <!-- one.upload mytest --> content <!-- --> ignore too", new DataService() {

            public void createNewNode(String value, String title, WhenNewNodeCreated callback) {
                System.out.println("Create node: "+title+" with "+value);
                callback.thenDo(One.reference("http://test.com"));
            }
        });
        
    }
}
