/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal.engine;

import aj.apps.microsync.internal.AjMicroSyncData;
import aj.apps.microsync.internal.DataService;
import aj.apps.microsync.internal.LogService;
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

    static final String commentRegex = "(// )?\\<![ \\r\\n\\t]*(--([^\\-]|[\\r\\n]|-[^\\-])*--[ \\r\\n\\t]*)\\>";

    public static enum Operation {

        NONE, UPLOADNEW, UPLOADPUBLIC, UPLOAD, DOWNLOAD
    };

    public interface WhenFilesProcessed {

        public void onSuccess();

        public void onFailure(Throwable t);
    }

    public interface WhenSyncComplete {

        public void onSuccess(String text);

        public void onFailure(Throwable t);
    }

    public static void processFile(final File inputFile, 
            final DataService dataService, 
            final LogService logService,
            final FileCache cache,
            final WhenFilesProcessed callback) throws Exception {

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
            
            
            logService.note("  Loading file: " + filePath);

            final FileInputStream fis = new FileInputStream(new File(
                    filePath));
            final Scanner scanner = new Scanner(fis, "UTF-8");

            String file = "";
            while (scanner.hasNextLine()) {
                file += scanner.nextLine() + "\n";
            }
            fis.close();

            final String fileClosed = file;
            logService.note("  Start processing file: " + filePath);
            processText(file, getExtension(filePath), dataService, cache.isModified(new File(filePath)),new WhenSyncComplete() {

                public void onSuccess(String text) {

                    if (!text.equals(fileClosed)) {
                        logService.note("  Writing file: " + filePath);
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
                    logService.note("  Processing completed for file: " + filePath);
                    latch.registerSuccess();
                }

                public void onFailure(Throwable t) {
                    latch.registerFail(t);
                }
            });


        }

    }

    public static void processText(String text, String extension, DataService service, final boolean skipUpload, final WhenSyncComplete callback) {
        final Pattern p = Pattern.compile(commentRegex);

        final Matcher matcher = p.matcher(text);

        new DoOperationsProcess(service, text, extension, matcher, skipUpload, new OperationCallback() {

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
        boolean skipUpload;
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
            final int commentContentStart;

            //System.out.println("Matched: "+file.substring(matcher.start()));
            if (!file.substring(matcher.start()).startsWith("// ")) {
                commentContentStart = matcher.start() + 4;
            } else {
                commentContentStart = matcher.start() + 7;
            }
            final int commentContentEnd = matcher.end() - 4;

            final String commentContent = file.substring(commentContentStart,
                    commentContentEnd);

            final String endMarker = "one.end";

            final String uploadNew = "one.create";
            final String uploadPublic = "one.createPublic";
            final String upload = "one.upload";
            final String download = "one.download";
            final String ignore = "one.ignoreNext";


            final String content;
            if (commentContent.length() > 2) {
                content = file.substring(
                        commentContentStart + 1, commentContentEnd);
            } else {
                content = "";
            }

            if (content.startsWith(ignore)) {
                matcher.find();
                next();
                return;
            }

            if (content.startsWith(endMarker)) {

                if (operation == Operation.UPLOADNEW || operation == Operation.UPLOADPUBLIC) {

                    final String enclosedWithinComments = file.substring(
                            lastCommentEnd, commentStart);

                    dataService.createNewNode(enclosedWithinComments,
                            parameter,
                            extension,
                            operation == Operation.UPLOADPUBLIC,
                            new AjMicroSyncData.WhenNewNodeCreated() {

                                public void thenDo(OneNode newNode) {
                                    operation = Operation.NONE;
                                    String replacement = "<!-- one.upload " + newNode.getId() + " -->";
                                    if (file.substring(lastCommentStart).startsWith("// ")) {
                                        replacement = "// " + replacement;
                                    }
                                    replacements.add(new Replace(lastCommentStart, lastCommentEnd, replacement));
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

                if (operation == Operation.UPLOAD) {

                    if (skipUpload) {
                        operation = Operation.NONE;
                        next();
                        return;
                    }
                    
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

                if (operation == Operation.DOWNLOAD) {

                    final String localValue = file.substring(
                            lastCommentEnd, commentStart);
                    dataService.downloadChanges(localValue, parameter, new DataService.WhenChangesDownloaded() {

                        public void onUnchanged() {
                            operation = Operation.NONE;

                            next();
                        }

                        public void onChanged(String newValue) {
                            Replace replace = new Replace(lastCommentEnd, commentStart, newValue);
                            replacements.add(replace);

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

                if (content.startsWith(uploadPublic)) {
                    operation = Operation.UPLOADPUBLIC;
                    lastCommentEnd = commentEnd;
                    lastCommentStart = commentStart;
                    parameter = file.substring(commentContentStart + uploadPublic.length() + 2,
                            commentContentEnd);
                    next();
                    return;
                }

                if (content.startsWith(uploadNew)) {
                    operation = Operation.UPLOADNEW;
                    lastCommentEnd = commentEnd;
                    lastCommentStart = commentStart;
                    parameter = file.substring(commentContentStart + uploadNew.length() + 2,
                            commentContentEnd);
                    next();
                    return;
                }

                if (content.startsWith(upload)) {
                    operation = Operation.UPLOAD;
                    lastCommentEnd = commentEnd;
                    lastCommentStart = commentStart;
                    parameter = file.substring(commentContentStart + upload.length() + 2,
                            commentContentEnd);
                    next();
                    return;
                }

                if (content.startsWith(download)) {
                    operation = Operation.DOWNLOAD;
                    lastCommentEnd = commentEnd;
                    lastCommentStart = commentStart;
                    parameter = file.substring(commentContentStart + download.length() + 2,
                            commentContentEnd);
                    next();
                    return;
                }


            }

            next();

        }

        public DoOperationsProcess(final DataService service, final String file, String extension, final Matcher matcher, final boolean skipUpload,
                final OperationCallback callback) {
            super();
            this.dataService = service;
            this.file = file;
            this.extension = extension;
            this.matcher = matcher;
            this.skipUpload = skipUpload;
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
