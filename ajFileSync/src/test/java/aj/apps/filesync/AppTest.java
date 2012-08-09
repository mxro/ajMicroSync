package aj.apps.filesync;

import aj.apps.filesync.internal.DataService;
import aj.apps.filesync.internal.DataService.WhenChangesUploaded;
import aj.apps.filesync.internal.DataService.WhenNewNodeCreated;
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

    public void testUploadOperation() {
        OneJre.init();
        SyncEngine.processText("ignore <!-- one.upload mytest --> content <!-- --> ignore too", new DataService() {

            public void createNewNode(String value, String title, WhenNewNodeCreated callback) {
                //System.out.println("Create node: "+title+" with "+value);
                callback.thenDo(One.reference("http://test.com"));
            }

            public void uploadChanges(String enclosedWithinComments, String parameter, WhenChangesUploaded callback) {
               // System.out.println("Upload: "+parameter+" with "+enclosedWithinComments);
                callback.thenDo();
            }
        }, new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
                
            }

            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
        
    }
    
    public void testSyncOperation() {
        OneJre.init();
        SyncEngine.processText("ignore <!-- one.sync http://test.com/mynode --> some rather lengthy\n text. <!-- -->ignore too", new DataService() {

            public void createNewNode(String value, String title, WhenNewNodeCreated callback) {
                //System.out.println("Create node: "+title+" with "+value);
                callback.thenDo(One.reference("http://test.com"));
            }

            public void uploadChanges(String enclosedWithinComments, String parameter, WhenChangesUploaded callback) {
                //System.out.println("Upload: "+parameter+" with "+enclosedWithinComments);
                callback.thenDo();
            }
        }, new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
               
            }

            public void onFailure(Throwable t) {
               throw new RuntimeException(t);
            }
        });
        
    }
    
    public void testSyncUploadAndSyncOperation() {
        OneJre.init();
        SyncEngine.processText("ignore <!-- one.sync http://test.com/mynode --> some rather lengthy\n text. <!-- -->ignore<!-- one.upload newNode --> to create <!-- --> too", new DataService() {

            public void createNewNode(String value, String title, WhenNewNodeCreated callback) {
                System.out.println("Create node: "+title+" with "+value);
                callback.thenDo(One.reference("http://test.com"));
            }

            public void uploadChanges(String enclosedWithinComments, String parameter, WhenChangesUploaded callback) {
                System.out.println("Upload: "+parameter+" with "+enclosedWithinComments);
                callback.thenDo();
            }
        }, new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
                System.out.println(text);
            }

            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
        
    }
}
