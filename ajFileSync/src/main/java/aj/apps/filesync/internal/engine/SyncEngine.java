/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal.engine;

import aj.apps.filesync.internal.AjFileSyncData;
import aj.apps.filesync.internal.DataService;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import one.core.nodes.OneNode;

/**
 *
 * @author Max
 */
public class SyncEngine {

    static final String commentRegex = "\\<![ \\r\\n\\t]*(--([^\\-]|[\\r\\n]|-[^\\-])*--[ \\r\\n\\t]*)\\>";

    public static enum Operation {

        NONE, UPLOAD, SYNC
    };

    public static void processFile(File inputFile, DataService service) throws Exception {

        for (final String filePath : getFilesRecursively(inputFile.getAbsoluteFile())) {
            if (filePath.endsWith(".html")) {

                final FileInputStream fis = new FileInputStream(new File(
                        filePath));
                final Scanner scanner = new Scanner(fis, "UTF-8");

                String file = "";
                while (scanner.hasNextLine()) {
                    file += scanner.nextLine();
                }

                processText(file, service);
            }

        }

    }

    public static void processText(String text, DataService service) {
        final Pattern p = Pattern.compile(commentRegex);

                final Matcher matcher = p.matcher(text);
                
                new DoOperationsProcess(service, text, matcher, new OperationCallback() {

                    @Override
                    public void onSuccess(final String newFile) {
                        System.out.println("all done");
                        System.out.println("file: ");
                        System.out.println(newFile);
                    }
                }).start();
    }
    
    public static interface OperationCallback {

        public void onSuccess(String newFile);
    }

    public static class DoOperationsProcess {

        final Matcher matcher;
        String file;
        
        OperationCallback callback;
        List<Replace> replacements;
        int lastCommentEnd = -1;
        int lastCommentStart = -1;
        String parameter = "";
        Operation operation = Operation.NONE;
        private final DataService dataService;

        public void start() {

            next();

        }

        protected void next() {
            if (!matcher.find()) {
                
                callback.onSuccess(performReplacements(file, replacements));
                
                return;
            }

            final int commentStart = matcher.start();
            final int commentEnd = matcher.end();
            final int commentContentStart = matcher.start() + 4;
            final int commentContentEnd = matcher.end() - 4;

            final String commentContent = file.substring(commentContentStart,
                    commentContentEnd);

            if (operation == Operation.UPLOAD) {
                if (commentContent.length() == 0) {
                    final String enclosedWithinComments = file.substring(
                            lastCommentEnd, commentStart);

                    dataService.createNewNode(enclosedWithinComments, parameter, new AjFileSyncData.WhenNewNodeCreated() {

                        public void thenDo(OneNode newNode) {
                            operation = Operation.NONE;
                            replacements.add(new Replace(lastCommentStart, lastCommentEnd, "<-- one.sync " + newNode.getId() + " -->"));
                            next();
                        }

                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            operation = Operation.NONE;

                            next();
                        }
                    });



                    return;
                }
            }
            
            if (operation == Operation.SYNC) {
                if (commentContent.length() == 0) {
                    final String enclosedWithinComments = file.substring(
                            lastCommentEnd, commentStart);
                    dataService.uploadChanges(enclosedWithinComments, parameter, new DataService.WhenChangesUploaded() {

                        public void thenDo() {
                            operation = Operation.NONE;

                            next();
                        }

                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            operation = Operation.NONE;

                            next();
                        }
                    });
                }
                
            }
            
            if (operation == Operation.NONE) {

                if (commentContent.length() > 6) {
                    final String content = file.substring(
                            commentContentStart + 1, commentContentEnd);

                    if (content.startsWith("one.upload")) {
                        operation = Operation.UPLOAD;
                        lastCommentEnd = commentEnd;
                        lastCommentStart = commentStart;
                        parameter = file.substring(commentContentStart + "one.upload".length()+1,
                                commentContentEnd);
                    }

                    if (content.startsWith("one.sync")) {
                        operation = Operation.SYNC;
                        lastCommentEnd = commentEnd;
                        lastCommentStart = commentStart;
                        parameter = file.substring(commentContentStart + "one.sync".length()+1,
                                commentContentEnd);
                    }

                }
            }

            next();

        }

        public DoOperationsProcess(final DataService service, final String file, final Matcher matcher,
                final OperationCallback callback) {
            super();
            this.dataService = service;
            this.file = file;
            this.matcher = matcher;
            this.callback = callback;
            this.replacements = new ArrayList<Replace>();
        }
    }

    public static String performReplacements(String input, List<Replace> replacements) {
        
        String result = input;
        
        for (Replace r : replacements) {
            
            result = result.substring(0, r.from) + r.with + result.substring(r.to);
            
        }
        return result;
    }
    
    public static class Replace {

        public int from;
        public int to;
        public String with;

        public Replace(int from, int to, String with) {
            this.from = from;
            this.to = to;
            this.with = with;
        }
    }

    public static List<String> getFilesRecursively(final File dir) {
        final ArrayList<String> list = new ArrayList<String>(100);

        if (dir.isFile()) {
            list.add(dir.getAbsolutePath());
            return list;
        }

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    list.addAll(getFilesRecursively(f));
                } else {
                    list.add(f.getAbsolutePath());
                }
            }
        }
        return list;
    }
}
