/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal;

import one.core.domain.OneClient;
import one.core.dsl.CoreDsl;
import one.core.dsl.callbacks.WhenLoaded;
import one.core.dsl.callbacks.results.WithLoadResult;
import one.core.dsl.callbacks.results.WithUserRegisteredResult;

/**
 *
 * @author mroh004
 */
public class AjDataServiceFactory implements DataServiceFactory {

    CoreDsl dsl;
    WithUserRegisteredResult wurr;
    
    public void createDataService(final WhenDataServiceCreated callback) {
        final OneClient client = dsl.createClient();
        
        dsl.load(wurr.userNodeUri()).withSecret(wurr.userNodeSecret()).in(client).and(new WhenLoaded() {

            @Override
            public void thenDo(WithLoadResult<Object> wlr) {
                callback.thenDo(new AjMicroSyncData(client, wurr));
            }
        });
        
       
    }

    public AjDataServiceFactory(CoreDsl dsl, WithUserRegisteredResult wurr) {
        this.dsl = dsl;
        this.wurr = wurr;
    }
    
    
    
}
