package lispy;

/** Utility class to handle error messages. */
public class Error {
  /**
   * Create a new RuntimeException.
   *
   * @param msg message for the exceptions
   * @return RuntimeException
   */
  public static RuntimeException error(String msg) {
    return new RuntimeException(msg);
  }

  /**
   * Throw a RuntimeException if a condition is met.
   *
   * @param condition condition to evaluate
   * @param msg message for RuntimeException
   */
  public static void throwIf(boolean condition, String msg) {
    if (condition) throw new RuntimeException(msg);
  }
}
