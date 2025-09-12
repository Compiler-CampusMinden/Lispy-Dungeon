package nodes;

/** Inherit from this class to store important values when visiting a node in the parsed tree. */
public abstract class INode {
  /** TokenType of the node. */
  public String type;

  /**
   * Create a new node with the given type.
   *
   * @param type TokenType of the node.
   */
  public INode(String type) {
    this.type = type;
  }
}
