package lispy.values;

import static lispy.Error.error;
import static lispy.Error.throwIf;
import static lispy.values.Value.*;

import contrib.utils.EntityUtils;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/** Builtin functions for Lispy. */
public class Builtins {

  /** Support for list operations. */
  public static Map<String, Function<List<Value>, Value>> listsupport =
      Map.of(
          "list", ListVal::of,
          "cons",
              args -> {
                throwIf(args.size() != 2, "cons: expected two arguments");

                Value head = args.getFirst();
                ListVal tail = asList(args.getLast());
                List<Value> out = new ArrayList<>(tail.elements().size() + 1);
                out.add(head);
                out.addAll(tail.elements());
                return ListVal.of(out);
              },
          "head",
              args -> {
                throwIf(args.size() != 1, "head: expected one argument");

                ListVal l = asList(args.getFirst());
                throwIf(l.isEmpty(), "head: got empty list");
                return l.elements().getFirst();
              },
          "tail",
              args -> {
                throwIf(args.size() != 1, "tail: expected one argument");

                ListVal l = asList(args.getFirst());
                throwIf(l.isEmpty(), "tail: got empty list");
                return ListVal.of(l.elements().subList(1, l.elements().size()));
              },
          "empty?",
              args -> {
                throwIf(args.size() != 1, "empty?: expected one argument");

                ListVal l = asList(args.getFirst());
                return new BoolVal(l.isEmpty());
              },
          "length",
              args -> {
                throwIf(args.size() != 1, "length: expected one argument");

                ListVal l = asList(args.getFirst());
                return new NumVal(l.elements().size());
              },
          "append",
              args ->
                  ListVal.of(
                      args.stream()
                          .map(Value::asList)
                          .flatMap(l -> l.elements().stream())
                          .toList()),
          "nth",
              args -> {
                throwIf(args.size() != 2, "nth: expected two arguments");

                int i = asNum(args.getFirst());
                ListVal l = asList(args.getLast());
                throwIf(i < 0 || i >= l.elements().size(), "nth: index out of bounds");
                return l.elements().get(i);
              });

  /** Support for printing values. */
  public static Map<String, Function<List<Value>, Value>> print =
      Map.of(
          "print",
          args -> {
            String line = args.stream().map(Value::pretty).reduce((a, b) -> a + " " + b).orElse("");
            System.out.println(line);
            return args.isEmpty() ? new BoolVal(true) : args.getLast();
          });

  /** Support for comparing values. */
  public static Map<String, Function<List<Value>, Value>> logicsupport =
      Map.of(
          "not",
          args -> {
            throwIf(args.size() != 1, "not: expected one argument");
            return new BoolVal(!isTruthy(args.getFirst()));
          },
          "=",
          args -> {
            throwIf(args.isEmpty(), "=: expected at least one argument");

            if (args.size() == 1) return new BoolVal(true);
            Value res = args.getFirst();
            return new BoolVal(args.stream().skip(1).allMatch(v -> valueEquals(res, v)));
          },
          ">",
          args -> {
            throwIf(args.isEmpty(), ">: expected at least one argument");

            List<Integer> list = args.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) > 0));
          },
          "<",
          args -> {
            throwIf(args.isEmpty(), "<: expected at least one argument");

            List<Integer> list = args.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) < 0));
          });

  /** Support for arithmetic operations. */
  public static Map<String, Function<List<Value>, Value>> mathsupport =
      Map.of(
          "+",
          args -> {
            throwIf(args.isEmpty(), "+: expected at least one argument");
            return new NumVal(args.stream().map(Value::asNum).reduce(0, Integer::sum));
          },
          "-",
          args -> {
            throwIf(args.isEmpty(), "-: expected at least one argument");

            int res = asNum(args.getFirst());
            if (args.size() == 1) return new NumVal(-1 * res);
            return new NumVal(args.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a - b));
          },
          "*",
          args -> {
            throwIf(args.isEmpty(), "*: expected at least one argument");
            return new NumVal(args.stream().map(Value::asNum).reduce(1, (a, b) -> a * b));
          },
          "/",
          args -> {
            throwIf(args.isEmpty(), "/: expected at least one argument");

            int res = asNum(args.getFirst());
            if (args.size() == 1) return new NumVal(1 / res);
            return new NumVal(args.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a / b));
          });

  /**
   * Support for dungeon functions (like in PRODUS).
   *
   * <p>This is real shit from utils.BlocklyCommands (blockly project).
   */
  public static Map<String, Function<List<Value>, Value>> dungeonsupport =
      Map.of(
          "shoot",
          args -> {
            new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), 500)
                .execute(Game.hero().orElseThrow());

            return new BoolVal(true);
          },
          "move",
          args -> {
            throwIf(args.isEmpty(), "move: expected at least one argument");
            int steps = asNum(args.getFirst());

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
          args -> {
            throwIf(args.isEmpty(), "turn: expected at least one argument");

            Direction dir =
                switch (args.getFirst()) {
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
