/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal;

import one.core.domain.OneClient;
import one.core.dsl.CoreDsl;
import one.core.dsl.callbacks.results.WithUserRegisteredResult;

/**
 *
 * @author mroh004
 */
public class AjDataServiceFactory implements DataServiceFactory {

    
    CoreDsl dsl;
    WithUserRegisteredResult wurr;
    
    public DataService createDataService() {
        
        return new AjMicroSyncData(dsl.createClient(), wurr);
    }

    public AjDataServiceFactory(CoreDsl dsl, WithUserRegisteredResult wurr) {
        this.dsl = dsl;
        this.wurr = wurr;
    }
    
    
    
}
