package lispy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import lispy.interp.Env;
import lispy.interp.Interpreter;
import lispy.interp.Value;
import lispy.parser.*;
import org.junit.jupiter.api.Test;

class InterpreterTest {

  @Test
  public void testNumber() {
    // given: 42
    Expr p = new Expr.NumberLiteral(42);
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testString() {
    // given: "wuppieFluppie"
    Expr p = new Expr.StringLiteral("wuppieFluppie");
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("\"wuppieFluppie\"", Value.pretty(res));
  }

  @Test
  public void testBooleanTrue() {
    // given: true
    Expr p = new Expr.BoolLiteral(true);
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBooleanFalse() {
    // given: false
    Expr p = new Expr.BoolLiteral(false);
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testSymbol() {
    // given: wuppie
    Expr p = new Expr.SymbolExpr("wuppie");
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testSymbolLet() {
    // given: (let wuppie 5) wuppie
    List<Expr> p =
        List.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            new Expr.SymbolExpr("wuppie"));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testApplyNotDefined() {
    // given: (foo 1)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("foo"), new Expr.NumberLiteral(1));

    Env e = new Env();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testApplyMismatchNumParams() {
    // given: (foo 1)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("foo"), new Expr.NumberLiteral(1));

    Env e = new Env();
    e.define("foo", new Value.ClosureFn("foo", List.of(), null, e));

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testApply() {
    // given: (foo 1)
    // foo(a) = a
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("foo"), new Expr.NumberLiteral(1));

    Env e = new Env();
    e.define("foo", new Value.ClosureFn("foo", List.of("a"), new Expr.SymbolExpr("a"), e));

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("1", Value.pretty(res));
  }

  @Test
  public void testBuiltinOrTrue() {
    // given: (or false 42 7 "wuppie")
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("or"),
            new Expr.BoolLiteral(false),
            new Expr.NumberLiteral(42),
            new Expr.NumberLiteral(7),
            new Expr.StringLiteral("wuppie"));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBuiltinOrFalse() {
    // given: (or false)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("or"), new Expr.BoolLiteral(false));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testBuiltinAndTrue() {
    // given: (and true 42 7 "wuppie")
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("and"),
            new Expr.BoolLiteral(true),
            new Expr.NumberLiteral(42),
            new Expr.NumberLiteral(7),
            new Expr.StringLiteral("wuppie"));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBuiltinAndFalse() {
    // given: (and false)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("and"), new Expr.BoolLiteral(false));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testBuiltinIfTrue() {
    // given: (if (< 1 2) (print "true") (print "WUPPIE"))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("if"),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("<"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(2)),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("true")),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("WUPPIE")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("\"true\"", Value.pretty(res));
  }

  @Test
  public void testBuiltinIfFalse() {
    // given: (if (> 1 2) (print "true") (print "WUPPIE"))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("if"),
            Expr.ListExpr.of(
                new Expr.SymbolExpr(">"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(2)),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("true")),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.StringLiteral("WUPPIE")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("\"WUPPIE\"", Value.pretty(res));
  }

  @Test
  public void testBuiltinLetSymbol() {
    // given: (let wuppie 5) (print wuppie)
    List<Expr> p =
        List.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("wuppie")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testBuiltinLetSymbolMutation() {
    // given: (let wuppie 5) (let wuppie 42) (print wuppie)
    List<Expr> p =
        List.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(42)),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("wuppie")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testBuiltinLetFnPrint() {
    // given: (let wuppie 5) (let a 10) (let (foo a b) (print (+ a b wuppie)))
    List<Expr> p =
        List.of(
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
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("<fn foo>", Value.pretty(res));
  }

  @Test
  public void testBuiltinLetFnEval() {
    // given:
    // (let wuppie 5) (let a 10)
    // (let (foo a b) (print (+ a b wuppie))
    // (let c (foo 1 100))
    // (print c) (print wuppie) (print a))
    List<Expr> p =
        List.of(
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
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("10", Value.pretty(res));
  }

  @Test
  public void testBuiltinLetFnConst() {
    // given:
    // (let wuppie 5) (let a 10)
    // (let (foo ) (print (+ a wuppie))
    // (let c (foo ))
    // (print c) (print wuppie) (print a))
    List<Expr> p =
        List.of(
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("wuppie"),
                new Expr.NumberLiteral(5)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"), new Expr.SymbolExpr("a"), new Expr.NumberLiteral(10)),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                Expr.ListExpr.of(new Expr.SymbolExpr("foo")),
                Expr.ListExpr.of(
                    new Expr.SymbolExpr("print"),
                    Expr.ListExpr.of(
                        new Expr.SymbolExpr("+"),
                        new Expr.SymbolExpr("a"),
                        new Expr.SymbolExpr("wuppie")))),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("let"),
                new Expr.SymbolExpr("c"),
                Expr.ListExpr.of(new Expr.SymbolExpr("foo"))),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("c")),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("wuppie")),
            Expr.ListExpr.of(new Expr.SymbolExpr("print"), new Expr.SymbolExpr("a")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("10", Value.pretty(res));
  }

  @Test
  public void testBuiltinLetFnArity() {
    // given:
    // (let (foo a b) (print (+ a b wuppie))
    // (let c (foo 1))
    List<Expr> p =
        List.of(
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
  public void testBuiltinList() {
    // given: (list 42 7 "wuppie")
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("list"),
            new Expr.NumberLiteral(42),
            new Expr.NumberLiteral(7),
            new Expr.StringLiteral("wuppie"));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("(42 7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testBuiltinListCons() {
    // given: (cons true (tail (list 42 7 "wuppie")))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("cons"),
            new Expr.BoolLiteral(true),
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
    assertEquals("(true 7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testBuiltinListHead() {
    // given: (head (list 42 7 "wuppie"))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("head"),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("list"),
                new Expr.NumberLiteral(42),
                new Expr.NumberLiteral(7),
                new Expr.StringLiteral("wuppie")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testBuiltinListHeadSingle() {
    // given: (head (list 42))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("head"),
            Expr.ListExpr.of(new Expr.SymbolExpr("list"), new Expr.NumberLiteral(42)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("42", Value.pretty(res));
  }

  @Test
  public void testBuiltinListHeadEmpty() {
    // given: (head (list ))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("head"), Expr.ListExpr.of(new Expr.SymbolExpr("list")));
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testBuiltinListTail() {
    // given: (tail (list 42 7 "wuppie"))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("tail"),
            Expr.ListExpr.of(
                new Expr.SymbolExpr("list"),
                new Expr.NumberLiteral(42),
                new Expr.NumberLiteral(7),
                new Expr.StringLiteral("wuppie")));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("(7 \"wuppie\")", Value.pretty(res));
  }

  @Test
  public void testBuiltinListTailSingle() {
    // given: (tail (list 42))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("tail"),
            Expr.ListExpr.of(new Expr.SymbolExpr("list"), new Expr.NumberLiteral(42)));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("()", Value.pretty(res));
  }

  @Test
  public void testBuiltinListTailEmpty() {
    // given: (tail (list ))
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("tail"), Expr.ListExpr.of(new Expr.SymbolExpr("list")));
    Env e = Interpreter.newGlobalEnv();

    // when, then
    assertThrows(RuntimeException.class, () -> Interpreter.eval(p, e));
  }

  @Test
  public void testBuiltinBoolNotFalse() {
    // given: (not false)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("not"), new Expr.BoolLiteral(false));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBuiltinBoolNotTrue() {
    // given: (not true)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("not"), new Expr.BoolLiteral(true));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testBuiltinBoolNotSomething() {
    // given: (not 1)
    Expr p = Expr.ListExpr.of(new Expr.SymbolExpr("not"), new Expr.NumberLiteral(1));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testBuiltinCompare() {
    // given: (= 1 1)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("="), new Expr.NumberLiteral(1), new Expr.NumberLiteral(1));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBuiltinCompareTypes() {
    // given: (= 1 "a")
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("="), new Expr.NumberLiteral(1), new Expr.StringLiteral("a"));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testBuiltinLessThan() {
    // given: (< 1 3)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("<"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(3));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("true", Value.pretty(res));
  }

  @Test
  public void testBuiltinGreaterThan() {
    // given: (> 1 3)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr(">"), new Expr.NumberLiteral(1), new Expr.NumberLiteral(3));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("false", Value.pretty(res));
  }

  @Test
  public void testBuiltinPlus() {
    // given: (+ 1 10 100 1000)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("+"),
            new Expr.NumberLiteral(1),
            new Expr.NumberLiteral(10),
            new Expr.NumberLiteral(100),
            new Expr.NumberLiteral(1000));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("1111", Value.pretty(res));
  }

  @Test
  public void testBuiltinMinus() {
    // given: (- 100 10 1)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("-"),
            new Expr.NumberLiteral(100),
            new Expr.NumberLiteral(10),
            new Expr.NumberLiteral(1));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("89", Value.pretty(res));
  }

  @Test
  public void testBuiltinMult() {
    // given: (* 100 10 2)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("*"),
            new Expr.NumberLiteral(100),
            new Expr.NumberLiteral(10),
            new Expr.NumberLiteral(2));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("2000", Value.pretty(res));
  }

  @Test
  public void testBuiltinDiv() {
    // given: (/ 100 10 2)
    Expr p =
        Expr.ListExpr.of(
            new Expr.SymbolExpr("/"),
            new Expr.NumberLiteral(100),
            new Expr.NumberLiteral(10),
            new Expr.NumberLiteral(2));
    Env e = Interpreter.newGlobalEnv();

    // when
    Value res = Interpreter.eval(p, e);

    // then
    assertEquals("5", Value.pretty(res));
  }

  @Test
  public void testRecursion() {
    // given: (let (fac n) (if (< n 2) 1 (* n (fac (- n 1))))) (fac 5)
    List<Expr> p =
        List.of(
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
    Value res = Interpreter.eval(p, e).getLast();

    // then
    assertEquals("120", Value.pretty(res));
  }
}
