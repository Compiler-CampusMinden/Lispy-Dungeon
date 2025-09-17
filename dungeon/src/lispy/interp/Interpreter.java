package lispy.interp;

import static lispy.interp.Value.*;
import static lispy.parser.Expr.*;
import static lispy.utils.Error.*;

import java.util.List;
import lispy.parser.*;

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
   * Evaluate an expression in a given environment (main interpreter dispatch).
   *
   * @param expr ast to evaluate
   * @param env environment to evaluate in
   * @return result of evaluation
   */
  public static Value eval(Expr expr, Env env) {
    return switch (expr) {
      case NumberLiteral n -> new NumVal(n.value());
      case StringLiteral s -> new StrVal(s.value());
      case BoolLiteral b -> new BoolVal(b.value());
      case SymbolExpr s -> env.get(s.name());
      case ListExpr list -> evalList(list, env);
    };
  }

  /**
   * Evaluate a list of expressions in a given environment.
   *
   * @param expr list of expressions to evaluate
   * @param env environment to evaluate in
   * @return list of results of evaluation
   */
  public static List<Value> eval(List<Expr> expr, Env env) {
    return expr.stream().map(e -> eval(e, env)).toList();
  }

  private static Value evalList(ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    throwIf(elems.isEmpty(), "cannot evaluate empty list");

    return switch (elems.getFirst()) {
      case SymbolExpr s -> evalList(s, list, env);
      default -> throw error("first element must be a symbol or op, got" + elems.getFirst());
    };
  }

  private static Value evalList(SymbolExpr headExpr, ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    throwIf(elems.isEmpty(), "cannot evaluate empty list");

    return switch (headExpr.name()) {
      case "let" -> evalLet(elems, env);
      case "if" -> evalIf(elems, env);
      case "while" -> evalWhile(elems, env);
      case "and" -> evalAnd(elems, env);
      case "or" -> evalOr(elems, env);
      default -> apply(headExpr, elems, env);
    };
  }

  private static Value evalLet(List<Expr> elems, Env env) {
    throwIf(elems.size() < 3, "let: too few arguments");

    return switch (elems.get(1)) {
      // variable: (let vname expr)
      case SymbolExpr nameSym -> evalLetVariable(nameSym, elems.get(2), env);
      // function: (let (fname p1 p2 ...) body)
      case ListExpr fnSig -> evalLetFunction(fnSig, elems.get(2), env);
      default -> throw error("let: expected '(let vname expr)' or '(let (fname p1 p2 ...) body)'");
    };
  }

  private static Value evalLetVariable(SymbolExpr nameSym, Expr expr, Env env) {
    // variable: (let vname expr)
    String vname = nameSym.name();
    Value val = eval(expr, env);
    env.define(vname, val);

    return val;
  }

  private static ClosureFn evalLetFunction(ListExpr fnSig, Expr body, Env env) {
    // function: (let (fname p1 p2 ...) body)
    List<Expr> sigElems = fnSig.elements();
    throwIf(sigElems.isEmpty(), "let: function signature expected (fname p1 p2 ...)");

    // get symbol names
    List<String> symbolnames =
        sigElems.stream()
            .map(
                e ->
                    switch (e) {
                      case SymbolExpr(String name) -> name;
                      default -> throw error("let: Expr.SymbolExpr expected");
                    })
            .toList();

    // fname and params
    String fname = symbolnames.getFirst();
    List<String> params = symbolnames.subList(1, symbolnames.size());

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
    Expr elseexpr = (elems.size() >= 4) ? elems.get(3) : new BoolLiteral(false);

    if (isTruthy(eval(cond, env))) res = eval(thenexpr, env);
    else if (elems.size() >= 4) res = eval(elseexpr, env);

    return res;
  }

  private static Value evalWhile(List<Expr> elems, Env env) {
    throwIf(elems.size() < 2, "expected '(while cond body)'");

    Value res = new BoolVal(false);
    Expr cond = elems.get(1);
    Expr body = elems.get(2);

    while (isTruthy(eval(cond, env))) res = eval(body, env);

    return res;
  }

  private static Value evalAnd(List<Expr> elems, Env env) {
    return new BoolVal(elems.stream().skip(1).map(e -> eval(e, env)).allMatch(Value::isTruthy));
  }

  private static Value evalOr(List<Expr> elems, Env env) {
    return new BoolVal(elems.stream().skip(1).map(e -> eval(e, env)).anyMatch(Value::isTruthy));
  }

  private static Value apply(SymbolExpr headExpr, List<Expr> elems, Env env) {
    // evaluate function name in current env
    FnVal fn =
        switch (eval(headExpr, env)) {
          case FnVal f -> f;
          default -> throw error("function expected: " + headExpr);
        };

    // apply function to args
    return fn.apply(elems.subList(1, elems.size()), env);
  }
}
