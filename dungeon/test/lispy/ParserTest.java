package lispy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import lispy.parser.*;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  public void testNumber() {
    // given
    List<Expr> res = List.of(new Expr.NumberLiteral(42));

    // when
    List<Expr> p = Parser.parseString(" 42 ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testString() {
    // given
    List<Expr> res = List.of(new Expr.StringLiteral("wuppieFluppie"));

    // when
    List<Expr> p = Parser.parseString(" \"wuppieFluppie\" ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testBooleanTrue() {
    // given
    List<Expr> res = List.of(new Expr.BoolLiteral(true));

    // when
    List<Expr> p = Parser.parseString(" true ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testBooleanFalse() {
    // given
    List<Expr> res = List.of(new Expr.BoolLiteral(false));

    // when
    List<Expr> p = Parser.parseString(" false ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testSymbol() {
    // given
    List<Expr> res = List.of(new Expr.SymbolExpr("wuppie"));

    // when
    List<Expr> p = Parser.parseString(" wuppie ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListID() {
    // given
    List<Expr> res =
        List.of(Expr.ListExpr.of(new Expr.SymbolExpr("wuppie"), new Expr.NumberLiteral(42)));

    // when
    List<Expr> p = Parser.parseString(" (wuppie, 42 ) ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListOP() {
    // given
    List<Expr> res =
        List.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("+"), new Expr.NumberLiteral(42), new Expr.NumberLiteral(7)));

    // when
    List<Expr> p = Parser.parseString(" (+ 42 7) ");

    // then
    assertEquals(res, p);
  }

  @Test
  public void testListIdBad() {
    // given, when, then
    assertThrows(RuntimeException.class, () -> Parser.parseString(" (\"wuppie\", 42 "));
  }

  @Test
  public void testComplexLine() {
    // given
    List<Expr> res =
        List.of(
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
    List<Expr> p =
        Parser.parseString(" (if (< 1 2) (do (print \"true\") (print \"WUPPIE\")) (print false)) ");

    // then
    assertEquals(res, p);
  }
}
