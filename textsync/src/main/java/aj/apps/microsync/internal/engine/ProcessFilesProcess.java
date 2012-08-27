/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.microsync.internal.engine;

import aj.apps.microsync.internal.DataService;
import aj.apps.microsync.internal.LogService;
import aj.apps.microsync.internal.engine.ParseTextProcess.ParseTextProcessParameters;
import aj.apps.microsync.internal.engine.ParseTextProcess.WhenSyncComplete;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import one.async.joiner.CallbackLatch;

/**
 *
 * @author mroh004
 */
public class ProcessFilesProcess {

    public interface WhenFilesProcessed {

        public void onSuccess();

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

            // logService.note("  Loading file: " + filePath);

            final FileInputStream fis = new FileInputStream(new File(
                    filePath));
            final Scanner scanner = new Scanner(fis, "UTF-8");

            String file = "";
            while (scanner.hasNextLine()) {
                file += scanner.nextLine() + "\n";
            }
            fis.close();

            final String fileClosed = file;
            //logService.note("  Start processing file: " + filePath);

            ParseTextProcessParameters params = new ParseTextProcessParameters() {

                public String text() {
                    return fileClosed;
                }

                public String extension() {
                    return getExtension(filePath);
                }

                public DataService dataService() {
                    return dataService;
                }

                public LogService logService() {
                    return logService;
                }

                public boolean skipUpload() {
                    return !cache.isModified(new File(filePath));
                }

                public WhenSyncComplete callback() {
                    return new ParseTextProcess.WhenSyncComplete() {

                        public void onSuccess(String text) {

                            if (!text.equals(fileClosed)) {

                                //logService.note("  Writing changed file: " + filePath);
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
                            // logService.note("  Processing completed for file: " + filePath);
                            latch.registerSuccess();
                        }

                        public void onFailure(Throwable t) {
                            latch.registerFail(t);
                        }
                    };
                }
            };

            ParseTextProcess.processText(params);


        }

    }

    private static List<String> getFilesRecursively(final File dir) {
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

    private static String getExtension(String path) {
        final int idx = path.lastIndexOf(".");
        return path.substring(idx + 1);
    }
}
