package lispy.interp;

import static lispy.interp.Interpreter.eval;
import static lispy.interp.Value.*;
import static lispy.utils.Error.error;
import static lispy.utils.Error.throwIf;

import contrib.utils.EntityUtils;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Game;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import lispy.parser.Expr;

/** Builtin functions for Lispy. */
public class Builtins {

  /** Support for control flow operations. */
  public static Map<String, BiFunction<List<Expr>, Env, Value>> controlflow =
      Map.of(
          "or",
          (args, env) -> new BoolVal(eval(args, env).stream().anyMatch(Value::isTruthy)),
          "and",
          (args, env) -> new BoolVal(eval(args, env).stream().allMatch(Value::isTruthy)),
          "if",
          (args, env) -> {
            throwIf(args.size() < 2, "expected '(if cond then [else])'");

            if (isTruthy(eval(args.get(0), env))) return eval(args.get(1), env);
            else if (args.size() >= 3) return eval(args.get(2), env);
            else return new BoolVal(false);
          },
          "let",
          (args, env) -> {
            throwIf(args.size() < 2, "let: too few arguments");

            return switch (args.get(0)) {
              case Expr.SymbolExpr nameSym -> { // variable: (let vname expr)
                String vname = nameSym.name();
                Value val = eval(args.get(1), env);

                env.bind(vname, val);

                yield val;
              }
              case Expr.ListExpr fnSig -> { // function: (let (fname p1 p2 ...) body)
                // get symbol names
                List<String> names =
                    fnSig.elements().stream()
                        .map(
                            e ->
                                switch (e) {
                                  case Expr.SymbolExpr(String name) -> name;
                                  default -> throw error("let: Expr.SymbolExpr expected");
                                })
                        .toList();
                throwIf(names.isEmpty(), "let: function signature expected (fname p1 p2 ...)");

                // fname and params
                String fname = names.getFirst();
                List<String> params = names.size() > 1 ? names.subList(1, names.size()) : List.of();

                // define new function
                ClosureFn fn = new ClosureFn(fname, params, args.get(1), env);
                env.bind(fname, fn);

                yield fn;
              }
              default ->
                  throw error("let: expected '(let vname expr)' or '(let (fname p1 p2 ...) body)'");
            };
          });

  /** Support for list operations. */
  public static Map<String, BiFunction<List<Expr>, Env, Value>> listsupport =
      Map.of(
          "list",
          (args, env) -> new ListVal(eval(args, env)),
          "cons",
          (args, env) -> {
            throwIf(args.size() != 2, "cons: expected two arguments");
            List<Value> argvals = eval(args, env);

            List<Value> out = new ArrayList<>();
            out.add(argvals.getFirst());
            out.addAll(asList(argvals.getLast()).elements());
            return new ListVal(out);
          },
          "head",
          (args, env) -> {
            throwIf(args.size() != 1, "head: expected one argument");
            List<Value> argvals = eval(args, env);

            ListVal l = asList(argvals.getFirst());
            throwIf(l.isEmpty(), "head: got empty list");
            return l.elements().getFirst();
          },
          "tail",
          (args, env) -> {
            throwIf(args.size() != 1, "tail: expected one argument");
            List<Value> argvals = eval(args, env);

            ListVal l = asList(argvals.getFirst());
            throwIf(l.isEmpty(), "tail: got empty list");
            return new ListVal(l.elements().subList(1, l.elements().size()));
          },
          "empty?",
          (args, env) -> {
            throwIf(args.size() != 1, "empty?: expected one argument");
            List<Value> argvals = eval(args, env);

            return new BoolVal(asList(argvals.getFirst()).isEmpty());
          },
          "length",
          (args, env) -> {
            throwIf(args.size() != 1, "length: expected one argument");
            List<Value> argvals = eval(args, env);

            return new NumVal(asList(argvals.getFirst()).elements().size());
          },
          "append",
          (args, env) -> {
            List<Value> argvals = eval(args, env);

            return new ListVal(
                argvals.stream().map(Value::asList).flatMap(l -> l.elements().stream()).toList());
          },
          "nth",
          (args, env) -> {
            throwIf(args.size() != 2, "nth: expected two arguments");
            List<Value> argvals = eval(args, env);

            int i = asNum(argvals.getFirst());
            ListVal l = asList(argvals.getLast());
            throwIf(i < 0 || i >= l.elements().size(), "nth: index out of bounds");
            return l.elements().get(i);
          });

  /** Support for printing values. */
  public static Map<String, BiFunction<List<Expr>, Env, Value>> print =
      Map.of(
          "print",
          (args, env) -> {
            List<Value> argvals = eval(args, env);

            String line =
                argvals.stream().map(Value::pretty).reduce((a, b) -> a + " " + b).orElse("");
            System.out.println(line);
            return argvals.isEmpty() ? new BoolVal(true) : argvals.getLast();
          });

  /** Support for comparing values. */
  public static Map<String, BiFunction<List<Expr>, Env, Value>> logicsupport =
      Map.of(
          "not",
          (args, env) -> {
            throwIf(args.size() != 1, "not: expected one argument");
            List<Value> argvals = eval(args, env);

            return new BoolVal(!isTruthy(argvals.getFirst()));
          },
          "=",
          (args, env) -> {
            throwIf(args.isEmpty(), "=: expected at least one argument");
            List<Value> argvals = eval(args, env);

            if (argvals.size() == 1) return new BoolVal(true);
            Value res = argvals.getFirst();
            return new BoolVal(argvals.stream().skip(1).allMatch(v -> valueEquals(res, v)));
          },
          ">",
          (args, env) -> {
            throwIf(args.isEmpty(), ">: expected at least one argument");
            List<Value> argvals = eval(args, env);

            List<Integer> list = argvals.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) > 0));
          },
          "<",
          (args, env) -> {
            throwIf(args.isEmpty(), "<: expected at least one argument");
            List<Value> argvals = eval(args, env);

            List<Integer> list = argvals.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) < 0));
          });

  /** Support for arithmetic operations. */
  public static Map<String, BiFunction<List<Expr>, Env, Value>> mathsupport =
      Map.of(
          "+",
          (args, env) -> {
            throwIf(args.isEmpty(), "+: expected at least one argument");
            List<Value> argvals = eval(args, env);

            return new NumVal(argvals.stream().map(Value::asNum).reduce(0, Integer::sum));
          },
          "-",
          (args, env) -> {
            throwIf(args.isEmpty(), "-: expected at least one argument");
            List<Value> argvals = eval(args, env);

            int res = asNum(argvals.getFirst());
            if (argvals.size() == 1) return new NumVal(-1 * res);
            return new NumVal(
                argvals.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a - b));
          },
          "*",
          (args, env) -> {
            throwIf(args.isEmpty(), "*: expected at least one argument");
            List<Value> argvals = eval(args, env);

            return new NumVal(argvals.stream().map(Value::asNum).reduce(1, (a, b) -> a * b));
          },
          "/",
          (args, env) -> {
            throwIf(args.isEmpty(), "/: expected at least one argument");
            List<Value> argvals = eval(args, env);

            int res = asNum(argvals.getFirst());
            if (argvals.size() == 1) return new NumVal(1 / res);
            return new NumVal(
                argvals.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a / b));
          });

  /**
   * Support for dungeon functions (like in PRODUS).
   *
   * <p>This is real shit from utils.BlocklyCommands (blockly project).
   */
  public static Map<String, BiFunction<List<Expr>, Env, Value>> dungeonsupport =
      Map.of(
          "shoot",
          (args, env) -> {
            new FireballSkill(SkillTools::cursorPositionAsPoint, 1, 15f, Integer.MAX_VALUE, 1, true)
                .execute(Game.hero().orElseThrow());
            return new BoolVal(true);
          },
          "move",
          (args, env) -> {
            throwIf(args.isEmpty(), "move: expected at least one argument");

            int steps = asNum(eval(args, env).getFirst());

            Vector2 newForce =
                Vector2.of(10 * steps, 10 * steps).scale(EntityUtils.getHeroViewDirection());

            Game.hero()
                .flatMap(h -> h.fetch(VelocityComponent.class))
                .map(
                    vc -> {
                      vc.applyForce(
                          "Movement",
                          vc.force("Movement")
                              .map(existing -> existing.add(newForce))
                              .orElse(newForce));
                      return vc;
                    });

            return new BoolVal(true);
          },
          "turn",
          (args, env) -> {
            throwIf(args.isEmpty(), "turn: expected at least one argument");

            Direction dir =
                switch (eval(args, env).getFirst()) {
                  case StrVal s ->
                      switch (s.value()) {
                        case "left" -> Direction.LEFT;
                        case "right" -> Direction.RIGHT;
                        case "up" -> Direction.UP;
                        case "down" -> Direction.DOWN;
                        default ->
                            throw error("turn: directions are 'up', 'left', 'down', 'right'");
                      };
                  default -> throw error("turn: string argument expected");
                };

            Game.hero()
                .flatMap(h -> h.fetch(VelocityComponent.class))
                .map(
                    vc -> {
                      vc.applyForce("Movement", dir);
                      return vc;
                    });

            return new BoolVal(true);
          });
}
