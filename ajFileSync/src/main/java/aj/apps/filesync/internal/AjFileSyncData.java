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
public class AjFileSyncData implements DataService {

    private final OneClient client;
    private final WithUserRegisteredResult loginDetails;

    public AjFileSyncData(OneClient client, WithUserRegisteredResult loginDetails) {
        this.client = client;
        this.loginDetails = loginDetails;
    }

    public void uploadChanges(final String newValue, String nodeUri, final WhenChangesUploaded callback) {
        
        final CoreDsl dsl = client.one();
        
        dsl.load(nodeUri).in(client).and(new WhenLoaded() {

            @Override
            public void thenDo(WithLoadResult<Object> wlr) {
                
                Object resolvedNode = dsl.dereference(wlr.loadedNode()).in(client);
                
                if (!(resolvedNode instanceof OneValue<?>)) {
                    callback.onFailure(new Exception("Node was not of type OneValue: "+wlr.loadedNode()));
                    return;
                }
                
                OneValue<?> valueNode = (OneValue<?>) resolvedNode;
                
                if (!(valueNode.getValue() instanceof String)) {
                    callback.onFailure(new Exception("Value nodes value was not of type String: "+valueNode));
                    return;
                }
                
                String oldValue = (String) valueNode.getValue();
                
                if (oldValue.equals(newValue)) {
                    callback.thenDo();
                    return;
                }
                
                OneValue<String> newValueNode = dsl.newNode(newValue).at(valueNode.getId());
                
                dsl.replaceSafe(valueNode).with(newValueNode).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {

                    @Override
                    public void thenDo(WithOperationResult<OneValue<String>> wor) {
                        callback.thenDo();
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
    public void createNewNode(final String value, final String title, final WhenNewNodeCreated callback) {


        assertAjFileSyncDataNode(new WhenSyncDataNodeAsserted() {

            public void thenDo(OneNode syncDataNode) {
                CoreDsl dsl = client.one();

                dsl.appendSafe(value).to(syncDataNode).atClosestAddress("./" + title).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {

                    @Override
                    public void thenDo(WithOperationResult<OneValue<String>> wor) {
                        callback.thenDo(wor.node());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        callback.onFailure(t);
                    }
                });


            }

            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });

    }

    private interface WhenSyncDataNodeAsserted {

        public void thenDo(OneNode syncDataNode);

        public void onFailure(Throwable t);
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

                                assertChild(wlr.loadedNode(), "docs", new WhenLoaded() {

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

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });



    }

    private void assertChild(final OneNode forNode, final String childPath, final WhenLoaded callback) {

        final CoreDsl dsl = client.one();

        dsl.load(forNode.getId() + "/" + childPath).in(client).and(new WhenLoaded() {

            @Override
            public void thenDo(WithLoadResult<Object> wlr) {
                callback.thenDo(wlr);
            }

            @Override
            public void onUndefined(WithUndefinedContext context) {

                dsl.appendSafe(childPath).to(forNode).atAddress("./" + childPath).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {

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
}
