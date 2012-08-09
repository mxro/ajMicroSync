/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal;

import one.core.domain.OneClient;
import one.core.dsl.CoreDsl;
import one.core.dsl.callbacks.WhenLoaded;
import one.core.dsl.callbacks.WhenResponseFromServerReceived;
import one.core.dsl.callbacks.results.*;
import one.core.nodes.OneNode;
import one.core.nodes.OneTypedReference;
import one.core.nodes.OneValue;

/**
 *
 * @author Max
 */
public class AjFileSyncData {
    
    private final OneClient client;
    private final WithUserRegisteredResult loginDetails;
    
    public AjFileSyncData(OneClient client, WithUserRegisteredResult loginDetails) {
        this.client = client;
        this.loginDetails = loginDetails;
    }
    
    private interface WhenSyncDataNodeAsserted {

        public void thenDo(OneNode syncDataNode);
        public void onFailure(Throwable t);
    }
    
    
    
    public 
    
    private void assertChild(final OneNode forNode, final String childPath, final WhenLoaded callback) {

        final CoreDsl dsl = client.one();
        
        dsl.load(forNode.getId()+"/"+childPath).in(client).and(new WhenLoaded() {

            @Override
            public void thenDo(WithLoadResult<Object> wlr) {
                callback.thenDo(wlr);
            }

            @Override
            public void onUndefined(WithUndefinedContext context) {
                
                dsl.appendSafe(childPath).to(forNode).atAddress("./"+childPath).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {

                    @Override
                    public void thenDo(WithOperationResult<OneValue<String>> wor) {
                       
                        callback.thenDo(new WithLoadResult<Object>(client, (OneTypedReference<Object>) dsl.reference(wor.node().getId())));
                        
                    }

                    @Override
                    public void onImpossible(WithImpossibleContext context) {
                        assertChild(forNode, childPath, callback);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        callback.onFailure(t);
                    }
                    
                    
                
                });
                
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
            
            
        });
        
        
    }
    
    private void assertAjFileSyncDataNode(final WhenSyncDataNodeAsserted callback) {
        
        CoreDsl dsl = client.one();
        
        dsl.load(loginDetails.userNodeUri()).withSecret(loginDetails.userNodeSecret()).in(client).and(new WhenLoaded() {
            
            @Override
            public void thenDo(WithLoadResult<Object> wlr) {
                
                assertChild(wlr.loadedNode(), "apps", new WhenLoaded() {

                    @Override
                    public void thenDo(WithLoadResult<Object> wlr) {
                        
                        assertChild(wlr.loadedNode(), "ajFileSync", new WhenLoaded() {

                            @Override
                            public void thenDo(WithLoadResult<Object> wlr) {
                               callback.thenDo(wlr.loadedNode());
                            }
                            
                            @Override
                    public void onFailure(Throwable t) {
                       callback.onFailure(t);
                    }
                        });
                        
                    }

                    @Override
                    public void onFailure(Throwable t) {
                       callback.onFailure(t);
                    }
                    
                    
                });
                
            }

            @Override
            public void onFailure(Throwable t) {
               callback.onFailure(t);
            }
            
            
        });
        
        
        
    }    
}
