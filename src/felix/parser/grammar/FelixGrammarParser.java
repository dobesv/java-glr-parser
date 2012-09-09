package felix.parser.grammar;

import static felix.parser.glr.grammar.Symbols.*;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.grammar.NonTerminal;
import felix.parser.glr.grammar.Priority;
import felix.parser.glr.grammar.Symbol;
import felix.parser.glr.grammar.SymbolRef;
import felix.parser.glr.grammar.Terminal;
import felix.parser.glr.parsetree.Node;
import felix.parser.glr.parsetree.Token;
import felix.parser.util.FilePos;
import felix.parser.util.ParserReader;

public class FelixGrammarParser {

//	Symbol
//	utstatement = nt("utstatement",
//			rule(kw("SCHEME", STRING)),
//			rule(kw("SAVE"))),
//	tstatement = nt("tstatement",
//			rule(utstatement, kw(";")),
//			// rule(kw("#line"), INTEGER, opt(STRING), newline) TODO
//			rule(kw("syntax"), NAME, kw("{"), dyprods, kw("}")),
//			rule(kw("open"), kw("syntax"), basic_name_comma_list, kw(";"))
//			),
//			
//	syntax_unit = nt("syntax_unit", zeroOrMore(tstatement))
//	
//	;
	
}
