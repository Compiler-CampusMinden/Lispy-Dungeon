package lispy.interp;

import static lispy.interp.Value.*;
import static lispy.parser.Expr.*;
import static lispy.utils.Error.*;

import java.util.*;
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
        .bind(Builtins.controlflow)
        .bind(Builtins.mathsupport)
        .bind(Builtins.logicsupport)
        .bind(Builtins.print)
        .bind(Builtins.listsupport)
        .bind(Builtins.dungeonsupport);
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
      case SymbolExpr s -> env.resolve(s.name());
      case ListExpr list -> apply(list, env);
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

  private static Value apply(ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    throwIf(elems.isEmpty(), "cannot evaluate empty list");

    // evaluate function name in current env
    Expr headExpr = elems.getFirst();
    FnVal fn =
        switch (eval(headExpr, env)) {
          case FnVal f -> f;
          default -> throw error("function expected: " + headExpr);
        };

    // apply function to args
    List<Expr> args = elems.size() > 1 ? elems.subList(1, elems.size()) : List.of();
    return fn.apply(args, env);
  }
}
