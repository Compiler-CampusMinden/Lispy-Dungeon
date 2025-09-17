package lispy;

import static org.junit.jupiter.api.Assertions.*;

import lispy.interp.Env;
import lispy.interp.Interpreter;
import lispy.interp.Value;
import lispy.parser.*;
import org.junit.jupiter.api.Test;

class InterpreterTest {

  @Test
  public void testNumber() {
    // given: 42
    Program p = Program.of(new Expr.NumberLiteral(42));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testString() {
    // given: "wuppieFluppie"
    Program p = Program.of(new Expr.StringLiteral("wuppieFluppie"));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("\"wuppieFluppie\"", Value.pretty(res));
  }

  @Test
  public void testBooleanTrue() {
    // given: true
    Program p = Program.of(new Expr.BoolLiteral(true));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBooleanFalse() {
    // given: false
    Program p = Program.of(new Expr.BoolLiteral(false));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testSymbol() {
    // given: wuppie
    Program p = Program.of(new Expr.SymbolExpr("wuppie"));
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testList() {
    // given: (list 42 7 "wuppie")
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("list"),
                new Expr.NumberLiteral(42),
                new Expr.NumberLiteral(7),
                new Expr.StringLiteral("wuppie")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("(42 7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testListHead() {
    // given: (head (list 42 7 "wuppie"))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("head"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("list"),
                    new Expr.NumberLiteral(42),
                    new Expr.NumberLiteral(7),
                    new Expr.StringLiteral("wuppie"))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testListHeadSingle() {
    // given: (head (list 42))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("head"),
                Expr.ListExpr.of(new Expr.SymbolExpr("list"), new Expr.NumberLiteral(42))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testListHeadEmpty() {
    // given: (head (list ))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("head"), Expr.ListExpr.of(new Expr.SymbolExpr("list"))));
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testListTail() {
    // given: (tail (list 42 7 "wuppie"))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("tail"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("list"),
                    new Expr.NumberLiteral(42),
                    new Expr.NumberLiteral(7),
                    new Expr.StringLiteral("wuppie"))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("(7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testListTailSingle() {
    // given: (tail (list 42))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("tail"),
                Expr.ListExpr.of(new Expr.SymbolExpr("list"), new Expr.NumberLiteral(42))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("()", Value.pretty(res));
  }

  @Test
  public void testListTailEmpty() {
    // given: (tail (list ))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("tail"), Expr.ListExpr.of(new Expr.SymbolExpr("list"))));
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testListCons() {
    // given: (cons true (tail (list 42 7 "wuppie")))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("cons"),
                new Expr.BoolLiteral(true),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("tail"),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("list"),
                        new Expr.NumberLiteral(42),
                        new Expr.NumberLiteral(7),
                        new Expr.StringLiteral("wuppie")))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("(true 7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testIfTrue() {
    // given: (if (< 1 2) (print "true") (print "WUPPIE"))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("if"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("<"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(2)),
                Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("true")),
                Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("WUPPIE"))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("\"true\"", Value.pretty(res));
  }

  @Test
  public void testIfFalse() {
    // given: (if (> 1 2) (print "true") (print "WUPPIE"))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("if"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr(">"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(2)),
                Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("true")),
                Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("WUPPIE"))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("\"WUPPIE\"", Value.pretty(res));
  }

  @Test
  public void testLetSymbol() {
    // given: (let wuppie 5) (print wuppie)
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("wuppie")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testLetFnPrint() {
    // given: (let wuppie 5) (let a 10) (let (foo a b) (print (+ a b wuppie)))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"), new Expr.SymbolExpr("a"), new Expr.NumberLiteral(10)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("foo"), new Expr.SymbolExpr("a"), new Expr.SymbolExpr("b")),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("print"),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("+"),
                        new Expr.SymbolExpr("a"),
                        new Expr.SymbolExpr("b"),
                        new Expr.SymbolExpr("wuppie")))));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("<fn foo>", Value.pretty(res));
  }

  @Test
  public void testLetFnEval() {
    // given:
    // (let wuppie 5) (let a 10)
    // (let (foo a b) (print (+ a b wuppie))
    // (let c (foo 1 100))
    // (print c) (print wuppie) (print a))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"), new Expr.SymbolExpr("a"), new Expr.NumberLiteral(10)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("foo"), new Expr.SymbolExpr("a"), new Expr.SymbolExpr("b")),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("print"),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("+"),
                        new Expr.SymbolExpr("a"),
                        new Expr.SymbolExpr("b"),
                        new Expr.SymbolExpr("wuppie")))),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("c"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("foo"),
                    new Expr.NumberLiteral(1),
                    new Expr.NumberLiteral(100))),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("c")),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("wuppie")),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("a")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("10", Value.pretty(res));
  }

  @Test
  public void testLetFnArity() {
    // given:
    // (let (foo a b) (print (+ a b wuppie))
    // (let c (foo 1))
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("foo"), new Expr.SymbolExpr("a"), new Expr.SymbolExpr("b")),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("print"),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("+"),
                        new Expr.SymbolExpr("a"),
                        new Expr.SymbolExpr("b"),
                        new Expr.SymbolExpr("wuppie")))),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("c"),
                Expr.ListExpr.of(new Expr.SymbolExpr("foo"), new Expr.NumberLiteral(1))));
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testBuildInPlus() {
    // given: (+ 1 10 100 1000)
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("+"),
                new Expr.NumberLiteral(1),
                new Expr.NumberLiteral(10),
                new Expr.NumberLiteral(100),
                new Expr.NumberLiteral(1000)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("1111", Value.pretty(res));
  }

  @Test
  public void testBuildInMinus() {
    // given: (- 100 10 1)
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("-"),
                new Expr.NumberLiteral(100),
                new Expr.NumberLiteral(10),
                new Expr.NumberLiteral(1)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("89", Value.pretty(res));
  }

  @Test
  public void testBuildInMult() {
    // given: (* 100 10 2)
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("*"),
                new Expr.NumberLiteral(100),
                new Expr.NumberLiteral(10),
                new Expr.NumberLiteral(2)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("2000", Value.pretty(res));
  }

  @Test
  public void testBuildInDiv() {
    // given: (/ 100 10 2)
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("/"),
                new Expr.NumberLiteral(100),
                new Expr.NumberLiteral(10),
                new Expr.NumberLiteral(2)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testRecursion() {
    // given: (let (fac n) (if (< n 2) 1 (* n (fac (- n 1))))) (fac 5)
    Program p =
        Program.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                Expr.ListExpr.of(new Expr.SymbolExpr("fac"), new Expr.SymbolExpr("n")),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("if"),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("<"),
                        new Expr.SymbolExpr("n"),
                        new Expr.NumberLiteral(2)),
                    new Expr.NumberLiteral(1),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("*"),
                        new Expr.SymbolExpr("n"),
                        Expr.ListExpr.of(
                            new Expr.SymbolExpr("fac"),
                            Expr.ListExpr.of(
                                new Expr.SymbolExpr("-"),
                                new Expr.SymbolExpr("n"),
                                new Expr.NumberLiteral(1)))))),
            Expr.ListExpr.of(new Expr.SymbolExpr("fac"), new Expr.NumberLiteral(5)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("120", Value.pretty(res));
  }
}
