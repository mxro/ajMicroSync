/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal.engine;

import aj.apps.filesync.internal.AjFileSyncData;
import aj.apps.filesync.internal.DataService;
import aj.apps.filesync.internal.LogService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import one.async.joiner.CallbackLatch;
import one.core.nodes.OneNode;
import one.utils.OneUtils;

/**
 *
 * @author Max
 */
public class SyncEngine {

    static final String commentRegex = "\\<![ \\r\\n\\t]*(--([^\\-]|[\\r\\n]|-[^\\-])*--[ \\r\\n\\t]*)\\>";

    public static enum Operation {

        NONE, UPLOAD, SYNC
    };

    public interface WhenFilesProcessed {

        public void onSuccess();

        public void onFailure(Throwable t);
    }

    public interface WhenSyncComplete {

        public void onSuccess(String text);

        public void onFailure(Throwable t);
    }

    public static void processFile(File inputFile, final DataService dataService, final LogService logService, final WhenFilesProcessed callback) throws Exception {

        final List<String> files = getFilesRecursively(inputFile.getAbsoluteFile());

        final CallbackLatch latch = new CallbackLatch(files.size()) {

            @Override
            public void onCompleted() {
                callback.onSuccess();
            }

            @Override
            public void onFailed(Throwable thrwbl) {
                callback.onFailure(thrwbl);
            }
        };


        for (final String filePath : files) {
            logService.note("Loading file: "+filePath);

            final FileInputStream fis = new FileInputStream(new File(
                    filePath));
            final Scanner scanner = new Scanner(fis, "UTF-8");

            String file = "";
            while (scanner.hasNextLine()) {
                file += scanner.nextLine();
            }
            fis.close();

            final String fileClosed = file;
             logService.note("Processing file: "+filePath);
            processText(file, getExtension(filePath), dataService, new WhenSyncComplete() {

                public void onSuccess(String text) {

                    if (!text.equals(fileClosed)) {
                         logService.note("Writing file: "+filePath);
                        try {
                            FileOutputStream fos = new FileOutputStream(new File(filePath));

                            byte[] data = text.getBytes("UTF-8");
                            fos.write(data, 0, data.length);

                            fos.close();
                        } catch (Exception e) {
                            latch.registerFail(e);
                            return;
                        }
                    }
                    logService.note("Processed file: " + filePath);
                    latch.registerSuccess();
                }

                public void onFailure(Throwable t) {
                    latch.registerFail(t);
                }
            });


        }

    }

    
    
    public static void processText(String text, String extension, DataService service, final WhenSyncComplete callback) {
        final Pattern p = Pattern.compile(commentRegex);

        final Matcher matcher = p.matcher(text);

        new DoOperationsProcess(service, text, extension, matcher, new OperationCallback() {

            @Override
            public void onSuccess(final String newFile) {
                callback.onSuccess(newFile);
                //System.out.println("all done");
                //System.out.println("file: ");
                // System.out.println(newFile);
            }
        }).start();
    }

    private final static String getExtension(String path) {
    final int idx = path.lastIndexOf(".");
    return path.substring(idx + 1);
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
        private final String extension;

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

                    dataService.createNewNode(enclosedWithinComments, parameter, extension, new AjFileSyncData.WhenNewNodeCreated() {

                        public void thenDo(OneNode newNode) {
                            operation = Operation.NONE;
                            replacements.add(new Replace(lastCommentStart, lastCommentEnd, "<!-- one.sync " + newNode.getId() + " -->"));
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

                    return;
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
                        parameter = file.substring(commentContentStart + "one.upload".length() + 2,
                                commentContentEnd);
                    }

                    if (content.startsWith("one.sync")) {
                        operation = Operation.SYNC;
                        lastCommentEnd = commentEnd;
                        lastCommentStart = commentStart;
                        parameter = file.substring(commentContentStart + "one.sync".length() + 2,
                                commentContentEnd);
                    }

                }

            }

            next();

        }

        public DoOperationsProcess(final DataService service, final String file, String extension, final Matcher matcher,
                final OperationCallback callback) {
            super();
            this.dataService = service;
            this.file = file;
            this.extension = extension;
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
