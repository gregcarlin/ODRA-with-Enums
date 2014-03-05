package odra.virtualnetwork.api;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
 
public class Console
    extends FilterOutputStream {
  private static ArrayList registeredListeners = new ArrayList();
 
  static {
    // On its first use. Install the Console Listener
    PrintStream printStream =
        new PrintStream(
            new Console(new ByteArrayOutputStream())
        );
    System.setOut(printStream);
  }
 
  public Console(OutputStream out) {
    super(out);
  }
 
  /* Override Ancestor method */
  public void write(byte b[]) throws IOException {
    String str = new String(b);
    logMessage(str);
  }
 
  /* Override Ancestor method */
  public void write(byte b[], int off, int len) throws IOException {
    String str = new String(b, off, len);
    logMessage(str);
  }
 
  /* Override Ancestor method */
  public void write(int b) throws IOException {
    String str = new String(new char[] { (char) b});
    logMessage(str);
  }
 
  public static void registerOutputListener(ConsoleListener listener) {
    // we don't register null listeners
    if (listener != null) {
      registeredListeners.add(listener);
    }
  }
 
  public static void removeOutputListener(ConsoleListener listener) {
    if (listener != null) {
      registeredListeners.remove(listener);
    }
  }
 
  private static void logMessage(String message) {
    // Log output to each listener
    int count = registeredListeners.size();
    for (int i = 0; i < count; i++) {
      ( (ConsoleListener) registeredListeners.get(i)).logMessage(message);
    }
  }
}
