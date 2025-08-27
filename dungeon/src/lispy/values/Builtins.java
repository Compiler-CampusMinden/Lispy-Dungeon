package lispy.values;

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
                if (args.size() != 2) throw new RuntimeException("cons: expected two arguments");

                Value head = args.getFirst();
                ListVal tail = Value.asList(args.getLast());
                List<Value> out = new ArrayList<>(tail.elements().size() + 1);
                out.add(head);
                out.addAll(tail.elements());
                return ListVal.of(out);
              },
          "head",
              args -> {
                if (args.size() != 1) throw new RuntimeException("head: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                if (l.isEmpty()) throw new RuntimeException("head: got empty list");
                return l.elements().getFirst();
              },
          "tail",
              args -> {
                if (args.size() != 1) throw new RuntimeException("tail: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                if (l.isEmpty()) throw new RuntimeException("tail: got empty list");
                return ListVal.of(l.elements().subList(1, l.elements().size()));
              },
          "empty?",
              args -> {
                if (args.size() != 1) throw new RuntimeException("empty?: expected one argument");

                ListVal l = Value.asList(args.getFirst());
                return new BoolVal(l.isEmpty());
              },
          "length",
              args -> {
                if (args.size() != 1) throw new RuntimeException("length: expected one argument");

                ListVal l = Value.asList(args.getFirst());
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
                if (args.size() != 2) throw new RuntimeException("nth: expected two arguments");

                int i = Value.asNum(args.getFirst());
                ListVal l = Value.asList(args.getLast());
                if (i < 0 || i >= l.elements().size())
                  throw new RuntimeException("nth: index out of bounds");
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
            if (args.size() != 1) throw new RuntimeException("not: expected one argument");
            return new BoolVal(!Value.isTruthy(args.getFirst()));
          },
          "=",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("=: expected at least one argument");

            if (args.size() == 1) return new BoolVal(true);
            Value res = args.getFirst();
            return new BoolVal(args.stream().skip(1).allMatch(v -> Value.valueEquals(res, v)));
          },
          ">",
          args -> {
            if (args.isEmpty()) throw new RuntimeException(">: expected at least one argument");

            List<Integer> list = args.stream().map(Value::asNum).toList();
            return new BoolVal(
                IntStream.range(1, list.size())
                    .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) > 0));
          },
          "<",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("<: expected at least one argument");

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
            if (args.isEmpty()) throw new RuntimeException("+: expected at least one argument");
            return new NumVal(args.stream().map(Value::asNum).reduce(0, Integer::sum));
          },
          "-",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("-: expected at least one argument");

            int res = Value.asNum(args.getFirst());
            if (args.size() == 1) return new NumVal(-1 * res);
            return new NumVal(args.stream().skip(1).map(Value::asNum).reduce(res, (a, b) -> a - b));
          },
          "*",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("*: expected at least one argument");
            return new NumVal(args.stream().map(Value::asNum).reduce(1, (a, b) -> a * b));
          },
          "/",
          args -> {
            if (args.isEmpty()) throw new RuntimeException("/: expected at least one argument");

            int res = Value.asNum(args.getFirst());
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
            if (args.isEmpty()) throw new RuntimeException("move: expected at least one argument");
            int steps = Value.asNum(args.getFirst());

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
            if (args.isEmpty()) throw new RuntimeException("turn: expected at least one argument");

            Direction dir =
                switch (args.getFirst()) {
                  case StrVal s ->
                      switch (s.value()) {
                        case "left" -> Direction.LEFT;
                        case "right" -> Direction.RIGHT;
                        case "up" -> Direction.UP;
                        case "down" -> Direction.DOWN;
                        default ->
                            throw new RuntimeException(
                                "turn: directions are 'up', 'left', 'down', 'right'");
                      };
                  default -> throw new RuntimeException("turn: string argument expected");
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
