package lispy.lexer;

/** Token type. */
public enum TokenType {
  /** '('. */
  LPAREN,
  /** ')'. */
  RPAREN,
  /** 'true'. */
  TRUE,
  /** 'false'. */
  FALSE,
  /** '[a-z][a-zA-Z0-9]*'. */
  ID,
  /** '[0-9]+'. */
  NUMBER,
  /** "'+' | '-' | '*' | '/' | '=' | '>' | '<'". */
  OP,
  /** '"' (~[\n\r"])* '"'. */
  STRING,
  /** eof. */
  EOF
}
