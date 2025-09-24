# Interpreter Demo Project

Dies ist ein Demo-Projekt. Es dient als Proof-of-Concept für einige Übungsaufgaben
in "Compilerbau" (Bachelor), gleichzeitig ist das der Kern der Demo für "Concepts of
Programming Languages" (Master).

Für eine einfache Lisp-ähnliche Sprache wird eine Grammatik angegeben und mit den
Mitteln aus der Vorlesung ein Lexer mit rekursivem Abstieg sowie ein einfacher
LL(1)-Parser mit ebenfalls rekursivem Abstieg implementiert. Der Parser erzeugt
einen AST, der für Listen Operationen und Symbole vereinfacht als Symbol speichert.
Der Interpreter traversiert den AST und wertet die einzelnen Expressions aus. Es
existiert ein Starter, der eine interaktive REPL ermöglicht, wobei auch das Vorladen
von größeren Programmen aus einer Datei möglich ist.

## Sprache

Ein Programm besteht aus einem oder mehreren Ausdrücken (*Expressions*). Die
Ausdrücke haben eine spezielle Form: Sie sind sogenannte
[S-Expressions](https://en.wikipedia.org/wiki/S-expression). Dies sind entweder
Literale der Form `x` oder einfache listenartige Gebilde der Form `(. x y)`, wobei
der `.` eine Operation (oder Funktion) darstellt und `x` und `y` selbst wieder
S-Expressions sind.

### S-Expressions

Die einfachste Form sind dabei Literale mit konkreten Werten der drei Datentypen
`Integer`, `String` und `Boolean`:

``` clojure
42          ;; Integer
"hello"     ;; String
true        ;; Boolean
false       ;; Boolean
```

Für eine Variable `foo` wäre das Folgende ebenfalls eine S-Expression:

``` clojure
foo         ;; Variable foo
```

(Über `;;` wird ein Kommentar eingeleitet, der bis zum Ende der Zeile geht.)

Komplexere Ausdrücke werden über die Listenform gebildet:

``` clojure
(+ 1 1)               ;; 1 + 1
(/ 10 3)              ;; 10 / 3
(+ 1 2 3 4)           ;; 1 + 2 + 3 + 4
(+ (+ (+ 1 2) 3) 4)   ;; (((1 + 2) + 3) + 4)
(/ (+ 10 2) (+ 2 4))  ;; ((10 + 2) / (2 + 4))
```

In der listenartigen Form ist der erste Eintrag der Liste immer eine Operation (oder
ein Funktionsname), danach kommen je nach Operation/Funktion (die Arität muss
passen!) entsprechende Einträge, die als Parameter für die Operation oder Funktion
zu verstehen sind.

Die Ausdrücke sind implizit von links nach rechts geklammert, d.h. der Ausdruck
`(+ 1 2 3 4)` ist [*syntactic sugar*](https://en.wikipedia.org/wiki/Syntactic_sugar)
für `(+ (+ (+ 1 2) 3) 4)`.

### Eingebaute Funktionen

Es gibt zwei Funktionen, die fest in der Sprache integriert sind.

Mit der eingebauten Funktion `print` kann der Wert eines Ausdrucks auf der Konsole
ausgegeben werden:

``` clojure
(print "hello world")
(print "wuppie\nfluppie\nfoo\nbar")
```

Die eingebaute Funktion `str` verknüpft ihre Argumente und bildet einen String.
Falls nötig, werden die Argumente vorher in einen String umgewandelt.[^1]

``` clojure
(str 42)                              ;; liefert "42" zurück
(str "wuppie" "fluppie" "foo" "bar")  ;; liefert "wuppiefluppiefoobar" zurück
(str "one: " 1 ", two: " 2)           ;; liefert "one: 1, two: 2" zurück
```

### Operatoren

Es gibt nur wenige vordefinierte Operatoren, diese mit der üblichen Semantik.

#### Vergleichsoperatoren

| Operation  | Operator |
|:-----------|:--------:|
| Gleichheit |   `=`    |
| Größer     |   `>`    |
| Kleiner    |   `<`    |

Die Operanden müssen jeweils beide den selben Typ haben. Dabei sind `String` und
`Integer` zulässig. Das Ergebnis ist immer vom Typ `Boolean`.

#### Logische Operatoren

| Operation | Operator |
|:----------|:--------:|
| Negation  |  `not`   |
| UND       |  `and`   |
| ODER      |   `or`   |

Die Operanden müssen jeweils beide den selben Typ haben. Dabei sind `String`,
`Integer` und `Boolean` zulässig. Das Ergebnis ist immer vom Typ `Boolean`.

#### Arithmetische Operatoren

| Operation      | Operator |
|:---------------|:--------:|
| Addition       |   `+`    |
| Subtraktion    |   `-`    |
| Multiplikation |   `*`    |
| Division       |   `/`    |

Die Operanden müssen jeweils beide den selben Typ haben. Dabei ist nur `Integer`
zulässig. Das Ergebnis ist vom Typ der Operanden.

### Kontrollstrukturen (If-Else)

Die `if-then-else`-Abfrage gibt es mit und ohne den `else`-Zweig:

``` clojure
(if boolean-form
    then-form
    optional-else-form)
```

``` clojure
(if (< 1 2)
    (print "true")    ;; then
    (print "false"))  ;; else
```

Dabei kann jeweils nur genau eine S-Expression genutzt werden. Wenn man mehrere
Dinge berechnen möchte, nutzt man `do`:[^1]

``` clojure
(do
    (print "wuppie")
    (print "fluppie")
    (print "foo")
    (print "bar"))
```

Beispiel:

``` clojure
(if (< 1 2) (do (print "true") (print "WUPPIE")) (print "false"))
```

oder anders formatiert:

``` clojure
(if (< 1 2)
    (do (print "true")
        (print "WUPPIE"))
    (print "false"))
```

### Schleifen

Es gibt einfache Schleifen mit `while`:

``` clojure
(while boolean-form
    body-form)
```

``` clojure
(while (< 1 2)
    (print "true"))
```

### Variablen: Bindings mit *let* anlegen

``` clojure
(let x 42)  ;; definiert eine neue Variable mit dem Namen "x" und dem Wert 42

x           ;; liefert 42
(+ x 7)     ;; liefert 49
```

### Funktionen mit *let* definieren

``` clojure
;;     name   params  body
(let  (hello  n)      (str "hello " n))  ;; Definition einer Funktion "hello" mit einem Parameter

(hello "world")                          ;; Aufruf der Funktion "hello" mit dem Argument "world"
```

### Lokale Scopes mit *let*[^1]

``` clojure
;;    bindings      use names here
(let  (name value)  (code that uses name))

(let x 99)   ;; globale Variable x
(let y 101)  ;; globale Variable y
(let z 42)   ;; globale Variable z
(let (x 1   ;; lokales x mit Wert 1(verdeckt globales x)
      y 2)  ;; lokales y mit Wert 2
     (+ x y z))  ;; 1+2+42

(let   hello
       (n)
       (let (l 42)  ;; l is valid in this scope
            (str "hello " n ": " l)
       )  ;; end of local scope
)  ;; end of function definition
```

Mit `let` können lokale Variablen erzeugt werden, die dann in dem jeweiligen Scope
genutzt werden können. Dies funktioniert wie in anderen Sprachen mit Scopes.

### Rekursion

``` clojure
(let (fac n)
    (if (< n 2)
        1
        (* n (fac (- n 1)))))
```

### Datenstrukturen

In unserer Sprache gibt es Listen:

``` clojure
(1 2 3)          ;; Fehler!
(let v (1 2 3))  ;; Fehler!
```

Das Problem daran ist, dass unsere S-Expressions zwar bereits listenartige
Strukturen sind, der erste Eintrag aber als Operator oder Funktion interpretiert
wird. Der Ausdruck oben würde beim Auswerten versuchen, die "Funktion" 1 auf den
Argumenten 2 und 3 aufzurufen ...

Man braucht also eine Notation, die ein sofortiges Auswerten verhindert und nur die
Liste an sich zurückliefert. Dies erreicht man durch die Funktion `list`:

``` clojure
(list 1 2 3)          ;; (1 2 3)

(let v (list 1 2 3))  ;; v = (1 2 3)
v                     ;; (1 2 3)
```

Mit der Funktion `nth` kann man auf das n-te Element einer Liste zugreifen:

``` clojure
(nth (list "abc" false 99) 2)  ;; 99
```

Zusätzlich gibt es die beiden Funktionen `head` und `tail`, die das erste Element
einer Liste bzw. die restliche Liste ohne das erste Element zurückliefern:

``` clojure
(head (list 1 2 3))  ;; 1
(tail (list 1 2 3))  ;; (2 3)
```

Mit der Funktion `cons` kann ein neues Element an eine Liste angefügt werden:

``` clojure
(cons true (tail (list 42 7 "wuppie")))  ;; (true 7 "wuppie")
```

## Interpreter

### Ein-/Ausgabe und Verarbeitung

Der Interpreter besteht aus einem Ein-/Ausgabe-Thread und dem Interpreter-Kern.

Der Ein-/Ausgabe-Thread liest von der Konsole zeilenweise ein und verpackt den
eingelesenen String in einen `Task` (Command-Pattern) und stellt diesen in die
synchronisierte Queue des Interpreters.

Der Interpreter läuft in einer Dauerschleife (gedacht als späteres `System` im
Dungeon) und holt sich den nächsten Task aus der Queue, verarbeitet diesen und
stellt das Ergebnis (oder den Fehler) in den Task und gibt diesen für den
Ein-/Ausgabe-Thread frei.

Der Ein-/Ausgabe-Thread wartet nach dem Einlesen, bis der Task abgearbeitet ist,
damit die Ausgabe vor dem nächsten Prompt erscheint. Der Ein-/Ausgabe-Thread fragt
nach der Vervollständigung der Eingabe, wenn die Klammern und/oder die
Anführungszeichen nicht balanciert sind.

### Interpreter

Der Interpreter-Kern besteht aus den drei Komponenten Lexer, Parser und Interpreter.

Der Lexer ist mit Hilfe von _recursive descent_ umgesetzt. Er erzeugt aus der
Eingabe auf Anfrage hin das nächste `Token`.

Die `Token` sind entsprechend der Lipsy-Grammatik modelliert.

Der Parser ist ein einfacher LL(1)-Parser mit _recursive descent_. Er erzeugt einen
AST, welcher entweder aus einem `Program` oder einer `Expr` besteht. Eine `Expr`
können die Literale (Zahl, String, Bool oder Symbol) sein. Zusätzlich gibt es auch
eine Expression, die eine Liste repräsentiert. Ein Program ist letztlich nur eine
Liste von Expressions, für die spätere Verarbeitung ist die explizite Modellierung
ganz nett, weil man so direkt über alle enthaltenen Expressions iterieren kann.

Es gibt keine semantische Analyse, d.h. es findet keine Typ-Prüfung statt und es
wird auch nicht geprüft, ob die verwendeten Namen in den Scopes jeweils bekannt sind
und ob Funktionen korrekt verwendet werden. Dies findet im Interpreter statt. Da
dieser in der REPL läuft und ohnehin immer zeilenweise arbeitet, lohnt sich eine
eigenständige semantische Analyse nicht bzw. die Fehler werden dann im Interpreter
geworfen.

Der Interpreter ist ein typischer AST-basierter Interpreter. Er läuft den AST ab und
wertet schrittweise die Expressions aus. Dabei werden verschachtelte Scopes
berücksichtigt, die mit Hilfe der Klasse `Env` realisiert werden.

Für die REPL wird eine globale `Env` angelegt und immer wieder mit in die Auswertung
übergeben. Frühere Auswertungen bleiben damit erhalten und können in späteren
Iterationen wieder verwendet werden.

Operatoren wie `+` oder Funktionen wie `print` werden als eingebaute Funktionen
realisiert. Sie werden beim Start in der globale `Env` vordefiniert.

Die Anbindung des Dungeon kann (a) über eingebaute Funktionen im Interpreter oder
(b) über die Modellierung des Dungeons als super-globales Environment umgesetzt
werden. Der Weg über die eingebauten Funktionen (und Symbole) ist der einfachere,
aber auch weniger flexible: Beispielsweise könnten Funktionen für die Interaktion
mit dem Hero eingebaut werden, ebenso könnte der Hero als Symbol mit Referenz auf
das Hero-Objekt im Dungeon in die globale Environment eingebaut werden. Damit ist
man dann aber genau auf diese Interaktionsmöglichkeiten beschränkt. Interessanter
wäre eine spezielle Environment, die als super-globales Environment agiert und die
die Symbole schließlich im Dungeon (also in `Game`) auflöst und damit nahezu
beliebige Interaktionen mit dem Dungeon erlaubt.

### Grammatik

Der Interpreter basiert auf der folgenden Grammatik:

``` antlr
grammar Lispy;


// Parser
program :  expr+ EOF ;


expr    :  literal
        |  symbol
        |  list
        ;

literal :  NUMBER
        |  STRING
        |  TRUE
        |  FALSE
        ;

symbol  : ID ;

list    : '(' (ID | OP) expr* ')' ;


// Lexer
TRUE    :  'true' ;
FALSE   :  'false' ;
ID      :  [a-z][a-zA-Z0-9]* ;
NUMBER  :  [0-9]+ ;
OP      :  '+' | '-' | '*' | '/' | '=' | '>' | '<' ;
STRING  :  '"' (~[\n\r"])* '"' ;

COMMENT :  ';;' ~[\n\r]* -> skip ;
WS      :  [ ,\t\n\r]+ -> skip ;
```



[^1]: noch nicht implementiert ...
