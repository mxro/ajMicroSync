package aj.apps.microsync.tests;

import aj.apps.microsync.internal.DataService;
import aj.apps.microsync.internal.DataService.WhenChangesUploaded;
import aj.apps.microsync.internal.DataService.WhenNewNodeCreated;
import aj.apps.microsync.internal.engine.SyncEngine;
import junit.framework.Assert;
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
        SyncEngine.processText("ignore <!-- one.createPublic mytest --> content <!-- one.end --> ignore too", "txt", new DummyDataService(), false,new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
                //System.out.println(text);
            }

            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
        
    }
    
    public void testSyncOperation() {
        OneJre.init();
        SyncEngine.processText("ignore <!-- one.upload http://test.com/mynode --> some rather lengthy\n text. <!-- -->ignore too", "txt", new DummyDataService(), false,new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
               
            }

            public void onFailure(Throwable t) {
               throw new RuntimeException(t);
            }
        });
        
    }
    
    public void testSyncUploadAndSyncOperation() {
        OneJre.init();
        SyncEngine.processText("ignore <!-- one.upload http://test.com/mynode --> some rather lengthy\n text. <!-- -->ignore<!-- one.uploadNew newNode --> to create <!-- --> too", "txt", new DummyDataService(), false, new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
                //System.out.println(text);
            }

            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
        
    }
    
    public void testDownloadOperation() {
        OneJre.init();
        
        String baesText = "ignore<!-- one.download http://test.com/mynode -->download<!-- one.end -->ignore";
        SyncEngine.processText(baesText, "txt", new DummyDataService(), false, new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
                Assert.assertEquals("ignore<!-- one.download http://test.com/mynode -->download+<!-- one.end -->ignore", text);
            }

            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
        
    }
    
    public void testIgnoreOperation() {
        OneJre.init();
        
        String baesText = "ignore<!-- one.download http://test.com/mynode -->start<!-- one.ignoreNext --><!-- one.end -->end // <!-- one.end -->ignore";
        SyncEngine.processText(baesText, "txt", new DummyDataService(), false, new SyncEngine.WhenSyncComplete() {

            public void onSuccess(String text) {
               // System.out.println(text);
                Assert.assertEquals("ignore<!-- one.download http://test.com/mynode -->start<!-- one.ignoreNext --><!-- one.end -->end +// <!-- one.end -->ignore", text);
            }

            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
        
    }

    private static class DummyDataService implements DataService {

        public DummyDataService() {
        }

        public void createNewNode(String value, String title, String extension, boolean isPublic, WhenNewNodeCreated callback) {
            //System.out.println("Create node: "+title+" with "+value);
            callback.thenDo(One.reference("http://test.com"));
        }

        public void downloadChanges(String value, String nodeUri, WhenChangesDownloaded callback) {
          // System.out.println("download: "+nodeUri+" with "+value);
            callback.onChanged(value+"+");
        }

        
        
        public void uploadChanges(String enclosedWithinComments, String parameter, WhenChangesUploaded callback) {
            
            callback.thenDo();
        }
    }
}
