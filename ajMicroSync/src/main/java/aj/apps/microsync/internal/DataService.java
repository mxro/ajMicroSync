/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal;

import one.core.dsl.callbacks.WhenShutdown;
import one.core.nodes.OneNode;

/**
 *
 * @author mroh004
 */
public interface DataService {

    public interface WhenNewNodeCreated {

        public void thenDo(OneNode newNode);

        public void onFailure(Throwable t);
    }

    
    public void createNewNode(final String value, final String title, final String extension, boolean isPublic,final WhenNewNodeCreated callback);

    public interface WhenChangesUploaded {
        public void thenDo();
        
        public void onFailure(Throwable t);
    }
    
    public void uploadChanges(String value, String nodeUri, WhenChangesUploaded callback);
    
    
    public interface WhenChangesDownloaded {
        
        public void onUnchanged();
        
        public void onChanged(String newValue);
        
        public void onFailure(Throwable t);
        
    }
    
    public void downloadChanges(String value, String nodeUri, WhenChangesDownloaded callback);
    
    public void shutdown(WhenShutdown callback);
    
}
