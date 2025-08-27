package lispy.values;

import contrib.components.BlockComponent;
import contrib.components.PathComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
          "shootFireball",
          args -> {
            Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
            new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), 500).execute(hero);

            return new BoolVal(true);
          },
          "move",
          args -> {
            Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
            Direction viewDirection = EntityUtils.getViewDirection(hero);
              VelocityComponent vc =
                hero
                  .fetch(VelocityComponent.class)
                  .orElseThrow(
                    () -> MissingComponentException.build(hero, VelocityComponent.class));

              Optional<Vector2> existingForceOpt = vc.force("Movement");
              Vector2 newForce = Vector2.of(5, 5).scale(viewDirection);

              Vector2 updatedForce =
                existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

              if (updatedForce.lengthSquared() > 0) {
                updatedForce = updatedForce.normalize().scale(Vector2.of(5, 5).length());
              }
            vc.applyForce("Movement", updatedForce);

            return new BoolVal(true);
          },
          "turnleft",
          args -> {
            Entity entity = Game.hero().orElseThrow(MissingHeroException::new);
            PositionComponent pc =
                entity
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, PositionComponent.class));
            VelocityComponent vc =
                entity
                    .fetch(VelocityComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, VelocityComponent.class));
            Point oldP = pc.position();
            vc.applyForce("Movement", Direction.LEFT);
            // so the player can not glitch inside the next tile
            pc.position(oldP);

            return new BoolVal(true);
          },
          "turnright",
          args -> {
            Entity entity = Game.hero().orElseThrow(MissingHeroException::new);
            PositionComponent pc =
                entity
                    .fetch(PositionComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, PositionComponent.class));
            VelocityComponent vc =
                entity
                    .fetch(VelocityComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, VelocityComponent.class));
            Point oldP = pc.position();
            vc.applyForce("Movement", Direction.RIGHT);
            // so the player can not glitch inside the next tile
            pc.position(oldP);

            return new BoolVal(true);
          });

}
