package lispy.interp;

import static lispy.interp.Value.*;
import static lispy.utils.Error.error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import lispy.parser.Expr;

/** memory model for interpreter. */
public class Env {
  private Env enclosing;
  private Map<String, Value> values = new HashMap<>();

  /** New blank environment (top level). */
  public Env() {
    this.enclosing = null;
  }

  /**
   * New blank environment with link to parent environment.
   *
   * @param parent enclosing scope
   */
  public Env(Env parent) {
    this.enclosing = parent;
  }

  /**
   * bind a name to a value.
   *
   * @param name name
   * @param value value
   * @return this environment (for chaining operations)
   */
  public Env bind(String name, Value value) {
    values.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    return this;
  }

  /**
   * bind a name to a value.
   *
   * @param builtins map of name/value pairs
   * @return this environment (for chaining operations)
   */
  public Env bind(Map<String, BiFunction<List<Expr>, Env, Value>> builtins) {
    builtins.forEach((n, fn) -> bind(n, new BuiltinFn(n, fn)));
    return this;
  }

  /**
   * retrieve a value to a name.
   *
   * @param name name
   * @return value
   */
  public Value resolve(String name) {
    if (values.containsKey(name)) return values.get(name);
    else if (enclosing != null) return enclosing.resolve(name);
    else throw error("unbound symbol: " + name);
  }
}
