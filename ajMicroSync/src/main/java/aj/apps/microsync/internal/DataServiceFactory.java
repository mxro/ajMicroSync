/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal;

/**
 *
 * @author mroh004
 */
public interface DataServiceFactory {
    
    public interface WhenDataServiceCreated {
        public void thenDo(DataService service);
        public void onFailure(Throwable t);
    }
    
    public void createDataService(WhenDataServiceCreated callback);
    
}
