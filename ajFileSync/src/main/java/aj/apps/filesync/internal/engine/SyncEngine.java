/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aj.apps.filesync.internal.engine;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Max
 */
public class SyncEngine {
    
    static final String commentRegex = "\\<![ \\r\\n\\t]*(--([^\\-]|[\\r\\n]|-[^\\-])*--[ \\r\\n\\t]*)\\>";
    
    public static enum Operation {
		NONE, UPLOAD, SYNC
	};
    
    public static void processFile(File inputFile) throws Exception {
        
        for (final String filePath : getFilesRecursively(inputFile.getAbsoluteFile())) {
			if (filePath.endsWith(".html")) {

				final FileInputStream fis = new FileInputStream(new File(
						filePath));
				final Scanner scanner = new Scanner(fis, "UTF-8");

				String file = "";
				while (scanner.hasNextLine()) {
					file += scanner.nextLine();
				}

				final Pattern p = Pattern.compile(commentRegex);

				final Matcher matcher = p.matcher(file);

				new DoOperationsProcess(file, matcher, new OperationCallback() {

					@Override
					public void onSuccess(final String newFile) {
						System.out.println("all done");
						System.out.println("file: ");
						System.out.println(newFile);
					}
				}).start();
			}

		}
        
    }
    
    
    public static interface OperationCallback {
		public void onSuccess(String newFile);
	}

	public static class DoOperationsProcess {

		final Matcher matcher;
		String file;
		OperationCallback callback;

		int lastCommentEnd = -1;
		int lastCommentStart = -1;
		String parameter = "";
		Operation operation = Operation.NONE;

		public void start() {

			next();

		}

		protected void next() {
			if (!matcher.find()) {
				callback.onSuccess(file);
				return;
			}

			final int commentStart = matcher.start();
			final int commentEnd = matcher.end();
			final int commentContentStart = matcher.start() + 4;
			final int commentContentEnd = matcher.end() - 4;

			// final String comment = file.substring(commentStart, commentEnd);

			final String commentContent = file.substring(commentContentStart,
					commentContentEnd);

			if (operation == Operation.UPLOAD) {
				if (commentContent.length() == 0) {
					final String enclosedWithinComments = file.substring(
							lastCommentEnd, commentStart);

                                        
                                        
					One.createRealm(parameter).and(new When.RealmCreated() {

						@Override
						public void thenDo(final WithRealmCreatedResult r) {
							System.out.println("Created realm: " + r.root());
							System.out.println("with secret: " + r.secret());

							final OneValue<String> newValue = One.newNode(
									enclosedWithinComments)
									.at(r.root().getId());

							One.replace(r.root()).with(newValue).in(r.client());

							One.append(
									One.newNode(allSecret).asReadWriteToken())
									.to(r.root()).in(r.client());

							operation = Operation.NONE;

							next();
						}
					});

					return;
				}
			}

			if (operation == Operation.NONE) {

				if (commentContent.length() > 6) {
					final String contentStart = file.substring(
							commentContentStart + 1, commentContentStart + 4);

					if (contentStart.equals("one.upload")) {
						operation = Operation.UPLOAD;
						lastCommentEnd = commentEnd;
						lastCommentStart = commentStart;
						parameter = file.substring(commentContentStart + 5,
								commentContentEnd);
					}

					if (contentStart.equals("one.sync")) {
						operation = Operation.SYNC;
						lastCommentEnd = commentEnd;
						lastCommentStart = commentStart;
                                                parameter = file.substring(commentContentStart + 5,
								commentContentEnd);
					}

				}
			}

			next();

		}

		public DoOperationsProcess(final String file, final Matcher matcher,
				final OperationCallback callback) {
			super();
			this.file = file;
			this.matcher = matcher;
			this.callback = callback;
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
