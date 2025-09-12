package lispy.values;

import static lispy.Error.error;
import static lispy.Error.throwIf;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import lispy.Interpreter;
import lispy.ast.Expr;

/**
 * Representation of values.
 *
 * <p>we could just use Integer, String, Boolean ... however, this allows us to use a nice little
 * type (instead of Object) and to use pattern matching with switch/case
 */
public sealed interface Value
    permits Value.NumVal, Value.StrVal, Value.BoolVal, Value.ListVal, Value.FnVal {
  /**
   * Read value as Integer.
   *
   * @param v value
   * @return Integer
   */
  static Integer asNum(Value v) {
    return switch (v) {
      case NumVal(int value) -> value;
      default -> throw error("number expected, got: " + Value.pretty(v));
    };
  }

  /**
   * Read value as ListVal.
   *
   * @param v value
   * @return ListVal
   */
  static ListVal asList(Value v) {
    return switch (v) {
      case ListVal l -> l;
      default -> throw error("list expected, got: " + Value.pretty(v));
    };
  }

  /**
   * Read value in boolean context.
   *
   * @param v value
   * @return true, if (BoolVal and true) or other value type
   */
  static boolean isTruthy(Value v) {
    return switch (v) {
      case BoolVal(boolean value) -> value;
      default -> true;
    };
  }

  /**
   * Compare values.
   *
   * @param a lhs value
   * @param b rhs value
   * @return true if equal
   */
  static boolean valueEquals(Value a, Value b) {
    return switch (a) {
      case NumVal an ->
          switch (b) {
            case NumVal bn -> an.value() == bn.value();
            default -> false;
          };
      case StrVal as ->
          switch (b) {
            case StrVal bs -> as.value().equals(bs.value());
            default -> false;
          };
      case BoolVal ab ->
          switch (b) {
            case BoolVal bb -> ab.value() == bb.value();
            default -> false;
          };
      case ListVal al ->
          switch (b) {
            case ListVal bl -> {
              List<Value> aElems = al.elements();
              List<Value> bElems = bl.elements();
              yield aElems.size() == bElems.size()
                  && IntStream.range(0, aElems.size())
                      .allMatch(i -> valueEquals(aElems.get(i), bElems.get(i)));
            }
            default -> false;
          };

      case FnVal af -> af == b; // functions: use identity
    };
  }

  /**
   * do some pretty printing.
   *
   * @param v value
   * @return formatted string
   */
  static String pretty(Value v) {
    return switch (v) {
      case NumVal n -> Integer.toString(n.value());
      case StrVal s -> "\"" + s.value() + "\"";
      case BoolVal b -> Boolean.toString(b.value());
      case ListVal l -> l.toString();
      case BuiltinFn f -> "<builtin " + f.name() + ">";
      case ClosureFn f -> "<fn " + f.name() + ">";
    };
  }

  /**
   * Numeric value (actually int).
   *
   * @param value value (int)
   */
  record NumVal(int value) implements Value {}

  /**
   * String value.
   *
   * @param value value (string)
   */
  record StrVal(String value) implements Value {}

  /**
   * Boolean.
   *
   * @param value bool
   */
  record BoolVal(boolean value) implements Value {}

  /**
   * Lists.
   *
   * @param elements list of values
   */
  record ListVal(List<Value> elements) implements Value {
    /**
     * create a new list value.
     *
     * @param vs list of values
     * @return new list value
     */
    public static ListVal of(List<Value> vs) {
      return new ListVal(vs);
    }

    /**
     * is this list empty?
     *
     * @return true if empty
     */
    public boolean isEmpty() {
      return elements.isEmpty();
    }

    @Override
    public String toString() {
      return elements.stream()
          .map(Value::pretty)
          .reduce(
              new StringBuilder("("),
              (sb, s) -> sb.length() == 1 ? sb.append(s) : sb.append(' ').append(s),
              StringBuilder::append)
          .append(')')
          .toString();
    }
  }

  /** Functions in Lispy. */
  sealed interface FnVal extends Value permits BuiltinFn, ClosureFn {
    /**
     * Apply function to arguments.
     *
     * @param args list of arguments
     * @return function result
     */
    Value apply(List<Value> args);
  }

  /**
   * Builtin functions.
   *
   * @param name fn name
   * @param impl reference to implementation (lambda)
   */
  record BuiltinFn(String name, Function<List<Value>, Value> impl) implements FnVal {
    @Override
    public Value apply(List<Value> args) {
      return impl.apply(args);
    }
  }

  /**
   * User defined functions (in Lispy).
   *
   * @param name fn name
   * @param params list of param names
   * @param body expression (fn body)
   * @param closureEnv closure
   */
  record ClosureFn(String name, List<String> params, Expr body, Env closureEnv) implements FnVal {
    @Override
    public Value apply(List<Value> args) {
      throwIf(
          args.size() != params.size(),
          "arity mismatch when calling "
              + name
              + ": expected "
              + params.size()
              + ", got "
              + args.size());

      Env callenv = new Env(closureEnv);
      IntStream.range(0, params.size()).forEach(i -> callenv.define(params.get(i), args.get(i)));
      return Interpreter.eval(body, callenv);
    }
  }
}
