/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal;

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

    
    public void createNewNode(final String value, final String title, final WhenNewNodeCreated callback);

    public interface WhenChangesUploaded {
        public void thenDo();
        
        public void onFailure(Throwable t);
    }
    
    public void uploadChanges(String value, String nodeUri, WhenChangesUploaded callback);
    
}
