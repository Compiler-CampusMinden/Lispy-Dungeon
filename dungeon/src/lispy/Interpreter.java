package lispy;

import static lispy.Error.error;
import static lispy.Error.throwIf;

import java.util.List;
import lispy.ast.*;
import lispy.values.*;

/** Lispy interpreter. */
public class Interpreter {
  /**
   * create a new environment containing builtin functions.
   *
   * @return new environment containing builtin functions
   */
  public static Env newGlobalEnv() {
    return new Env()
        .define(Builtins.mathsupport)
        .define(Builtins.logicsupport)
        .define(Builtins.print)
        .define(Builtins.listsupport)
        .define(Builtins.dungeonsupport);
  }

  /**
   * Evaluate a program.
   *
   * @param program ast to evaluate
   * @return result of evaluation
   */
  public static Value eval(Program program) {
    return eval(program, newGlobalEnv());
  }

  /**
   * Evaluate a program in a given environment.
   *
   * @param program ast to evaluate
   * @param env environment to evaluate in
   * @return result of evaluation
   */
  public static Value eval(Program program, Env env) {
    return program.expressions().stream()
        .map(e -> eval(e, env))
        .reduce(new BoolVal(false), (prev, curr) -> curr);
  }

  /**
   * main interpreter dispatch.
   *
   * @param expr ast to evaluate
   * @param env environment to evaluate in
   * @return result of evaluation
   */
  public static Value eval(Expr expr, Env env) {
    return switch (expr) {
      case Expr.NumberLiteral n -> new NumVal(n.value());
      case Expr.StringLiteral s -> new StrVal(s.value());
      case Expr.BoolLiteral b -> new BoolVal(b.value());
      case Expr.SymbolExpr s -> env.get(s.name());
      case Expr.ListExpr list -> evalList(list, env);
    };
  }

  private static Value evalList(Expr.ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    throwIf(elems.isEmpty(), "cannot evaluate empty list");

    return switch (elems.getFirst()) {
      case Expr.SymbolExpr s -> evalList(s, list, env);
      default -> throw error("first element must be a symbol or op, got" + elems.getFirst());
    };
  }

  private static Value evalList(Expr.SymbolExpr headExpr, Expr.ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    throwIf(elems.isEmpty(), "cannot evaluate empty list");

    return switch (headExpr.name()) {
      case "let" -> evalLet(elems, env);
      case "if" -> evalIf(elems, env);
      case "while" -> evalWhile(elems, env);
      case "and" -> evalAnd(elems, env);
      case "or" -> evalOr(elems, env);
      default -> evalFn(headExpr, elems, env);
    };
  }

  private static Value evalLet(List<Expr> elems, Env env) {
    throwIf(elems.size() < 3, "let: too few arguments");

    return switch (elems.get(1)) {
      // variable: (let vname expr)
      case Expr.SymbolExpr nameSym -> evalLetVariable(nameSym, elems.get(2), env);
      // function: (let (fname p1 p2 ...) body)
      case Expr.ListExpr fnSig -> evalLetFunction(fnSig, elems.get(2), env);
      default -> throw error("let: expected '(let vname expr)' or '(let (fname p1 p2 ...) body)'");
    };
  }

  private static Value evalLetVariable(Expr.SymbolExpr nameSym, Expr expr, Env env) {
    // variable: (let vname expr)
    String vname = nameSym.name();
    Value val = eval(expr, env);
    env.define(vname, val);

    return val;
  }

  private static ClosureFn evalLetFunction(Expr.ListExpr fnSig, Expr body, Env env) {
    // function: (let (fname p1 p2 ...) body)
    List<Expr> sigElems = fnSig.elements();
    throwIf(sigElems.isEmpty(), "let: function signature expected (fname p1 p2 ...)");

    // fname
    String fname =
        switch (sigElems.getFirst()) {
          case Expr.SymbolExpr(String name) -> name;
          default -> throw error("let: function name needs to be lispy.ast.Expr.SymbolExpr");
        };

    // parameters
    List<String> params =
        sigElems.stream()
            .skip(1)
            .map(
                e ->
                    switch (e) {
                      case Expr.SymbolExpr(String name) -> name;
                      default ->
                          throw error(
                              "let: function parameters needs to be"
                                  + " lispy.ast.Expr.SymbolExpr");
                    })
            .toList();

    // define new function
    ClosureFn fn = new ClosureFn(fname, params, body, env);
    env.define(fname, fn);

    return fn;
  }

  private static Value evalIf(List<Expr> elems, Env env) {
    throwIf(elems.size() < 3, "expected '(if cond then [else])'");

    Value res = new BoolVal(false);
    Expr cond = elems.get(1);
    Expr thenexpr = elems.get(2);
    Expr elseexpr = (elems.size() >= 4) ? elems.get(3) : new Expr.BoolLiteral(false);

    if (Value.isTruthy(eval(cond, env))) res = eval(thenexpr, env);
    else if (elems.size() >= 4) res = eval(elseexpr, env);

    return res;
  }

  private static Value evalWhile(List<Expr> elems, Env env) {
    throwIf(elems.size() < 2, "expected '(while cond body)'");

    Value res = new BoolVal(false);
    Expr cond = elems.get(1);
    Expr body = elems.get(2);

    while (Value.isTruthy(eval(cond, env))) res = eval(body, env);

    return res;
  }

  private static Value evalAnd(List<Expr> elems, Env env) {
    return new BoolVal(elems.stream().skip(1).map(e -> eval(e, env)).allMatch(Value::isTruthy));
  }

  private static Value evalOr(List<Expr> elems, Env env) {
    return new BoolVal(elems.stream().skip(1).map(e -> eval(e, env)).anyMatch(Value::isTruthy));
  }

  private static Value evalFn(Expr.SymbolExpr headExpr, List<Expr> elems, Env env) {
    // evaluate function name in current env
    FnVal fn =
        switch (eval(headExpr, env)) {
          case FnVal f -> f;
          default -> throw error("function expected: " + headExpr);
        };

    // evaluate args in current env
    List<Value> args = elems.stream().skip(1).map(e -> eval(e, env)).toList();

    // apply function to args
    return fn.apply(args);
  }
}
