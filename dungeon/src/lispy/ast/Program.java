package lispy.ast;

import java.util.List;

/**
 * A programm represents a list of expressions.
 *
 * @param expressions expressions
 */
public record Program(List<Expr> expressions) {
  /**
   * create a new programm node.
   *
   * @param expressions expressions
   * @return program (containing expressions)
   */
  public static Program of(Expr... expressions) {
    return new Program(List.of(expressions));
  }
}
