package lispy.parser;

import java.util.List;

/** Base class for expressions. */
public sealed interface Expr
    permits Expr.NumberLiteral,
        Expr.StringLiteral,
        Expr.BoolLiteral,
        Expr.SymbolExpr,
        Expr.ListExpr {
  /**
   * Expressions: numbers.
   *
   * @param value value (int)
   */
  record NumberLiteral(int value) implements Expr {}

  /**
   * Expression: string.
   *
   * @param value string
   */
  record StringLiteral(String value) implements Expr {}

  /**
   * Expression: bool.
   *
   * @param value bool
   */
  record BoolLiteral(boolean value) implements Expr {}

  /**
   * Expression: symbol.
   *
   * @param name string
   */
  record SymbolExpr(String name) implements Expr {}

  /**
   * Expression: lists.
   *
   * @param elements expressions
   */
  record ListExpr(List<Expr> elements) implements Expr {
    /**
     * create a new list expression.
     *
     * @param elements expressions
     * @return list expression
     */
    public static ListExpr of(Expr... elements) {
      return new ListExpr(List.of(elements));
    }
  }
}
