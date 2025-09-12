package lispy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lispy.ast.*;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  public void testNumber() {
    // given
    Program res = Program.of(new Expr.NumberLiteral(42));

    // when
    Program p = Parser.parseString(" 42 ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testString() {
    // given
    Program res = Program.of(new Expr.StringLiteral("wuppieFluppie"));

    // when
    Program p = Parser.parseString(" \"wuppieFluppie\" ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testBooleanTrue() {
    // given
    Program res = Program.of(new Expr.BoolLiteral(true));

    // when
    Program p = Parser.parseString(" true ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testBooleanFalse() {
    // given
    Program res = Program.of(new Expr.BoolLiteral(false));

    // when
    Program p = Parser.parseString(" false ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testSymbol() {
    // given
    Program res = Program.of(new Expr.SymbolExpr("wuppie"));

    // when
    Program p = Parser.parseString(" wuppie ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListID() {
    // given
    Program res =
        Program.of(Expr.ListExpr.of(new Expr.SymbolExpr("wuppie"), new Expr.NumberLiteral(42)));

    // when
    Program p = Parser.parseString(" (wuppie, 42 ) ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListOP() {
    // given
    Program res =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("+"), new Expr.NumberLiteral(42), new Expr.NumberLiteral(7)));

    // when
    Program p = Parser.parseString(" (+ 42 7) ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListIdBad() {
    // given

    // when, then
    assertThrows(RuntimeException.class, () -> Parser.parseString(" (\"wuppie\", 42 "));
  }

  @Test
  public void testComplexLine() {
    // given
    Program res =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("if"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("<"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(2)),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("do"),
                    Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("true")),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("print"), new Expr.StringLiteral("WUPPIE"))),
                Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.BoolLiteral(false))));

    // when
    Program p =
        Parser.parseString(" (if (< 1 2) (do (print \"true\") (print \"WUPPIE\")) (print false)) ");

    // then
    assertEquals(res, p);
  }
}
