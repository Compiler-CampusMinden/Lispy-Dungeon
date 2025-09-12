package lispy.ast;

/** Base class for expressions. */
public sealed interface Expr
    permits NumberLiteral, StringLiteral, BoolLiteral, SymbolExpr, ListExpr {}
