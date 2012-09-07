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

public class DypgenGrammarParser extends GrammarParser {
	
	static class OcamlCodeConsumer extends Terminal {
		public OcamlCodeConsumer() {
			super("OCAML_CODE");
		}

		@Override
		public Node match(ParserReader input, StackHead head, String ignored)
				throws IOException {
			FilePos start = input.getFilePos();
			int startCh = input.read();
			// TODO Support a @ space* {
//			if(startCh == '@') {
//				startCh = input.read();
//				while(Character.isSpaceChar(startCh)) {
//					startCh = input.read();
//				}
//				if(startCh != '{') {
//					input.seek(start);
//					return null;
//				}
//			}
			
			boolean angle = startCh == '<';
			boolean curly = startCh == '{';
			if(!(angle || curly)) {
				input.seek(start);
				return null;
			}
			StringBuffer text = new StringBuffer();
			int curlyDepth = 0;
			int angleDepth = 0;
			text.append(startCh);
			for(;;) {
				if(input.remaining() == 0) {
					// EOF
					input.seek(start);
					return null;
				}
				final int ch = input.read();
				text.append((char)ch);
				switch(ch) {
				case '{':
					curlyDepth += 1;
					break;
				case '}':
					if(curly && curlyDepth == 0)
						return new Token(input.getFileRange(start), this, text.toString(), ignored);
					else
						curlyDepth -= 1;
					break;
				case '<':
					angleDepth += 1;
					break;
				case '>':
					if(angle && angleDepth == 0)
						return new Token(input.getFileRange(start), this, text.toString(), ignored);
					else
						angleDepth -= 1;
					break;
				case '"':
					consumeString(input, text);
					break;
				case '(':
					consumeComment(input, text);
					break;
				}
			}
		}

		private void consumeComment(ParserReader input, StringBuffer text)
				throws IOException {
			// Need to match comment starts/ends
			if(input.startsWith('*')) {
				text.append('*');
				int depth=0;
				for(;;) {
					final int commentCh = input.read();
					text.append((char)commentCh);
					if(commentCh == '*' && input.startsWith(')')) {
						text.append(')');
						if(depth == 0) {
							break;
						} else {
							depth -= 1;
						}
					} else if(commentCh == '(' && input.startsWith('*')) {
						text.append('*');
						depth += 1;
					}
				}
			}
		}

		private void consumeString(ParserReader input, StringBuffer text) throws IOException {
			boolean escape = false;
			for(;;) {
				final int ch = input.read();
				if(ch == -1) {
					return;
				}
				text.append((char)ch);
				if(escape) {
					escape = false;
				} else if(ch == '\\') {
					escape = true;
				}
			}
		}
	}
	
	Terminal 
		KWD_TOKEN = kw("KWD_TOKEN", "%token"),
		KWD_START = kw("KWD_START", "%start"),
		KWD_RELATION = kw("KWD_RELATION", "%relation"),
		KWD_MLITOP = kw("KWD_MLITOP", "%mlitop"),
		KWD_MLIMID = kw("KWD_MLIMID", "%mlimid"),
		KWD_MLTOP = kw("KWD_MLTOP", "%mltop"),
		KWD_MLI = kw("KWD_MLI", "%mli"),
		KWD_CONSTRUCTOR = kw("KWD_CONSTRUCTOR", "%constructor"),
		KWD_FOR = kw("KWD_FOR", "%for"),
		KWD_NON_TERMINAL = kw("KWD_NON_TERMINAL", "%non_terminal"),
		KWD_TYPE = kw("KWD_TYPE", "%type"),
		KWD_LAYOUT = kw("KWD_LAYOUT", "%layout"),
		KWD_MERGE = kw("KWD_MERGE", "%merge"),
		LET = kw("LET", "let"),
		KWD_LEXER = kw("KWD_LEXER", "%lexer"),
		KWD_PARSER = kw("KWD_PARSER", "%parser"),
		PERCENTPERCENT = kw("PERCENTPERCENT", "%%"),
		LIDENT = re("LIDENT", "\\p{Lower}\\w*"),
		UIDENT = re("UIDENT", "\\p{Upper}\\w*"),
		IDENT = re("IDENT", "\\p{Alpha}\\w*"),
		LPAREN = kw("LPAREN", "("),	
		LPARENLESS = re("LPARENLESS", "\\(\\s*<"),
		LPARENGREATER = re("LPARENGREATER", "\\(\\s*>"),
		RPAREN = kw("RPAREN", ")"),
		LBRACK = kw("LBRACK", "["),
		RBRACK = kw("RBRACK", "]"),
		CARET = kw("CARET", "^"),
		DASH = kw("DASH", "-"),
		COMMA = kw("COMMA", ","),
		SEMI = kw("SEMI", ";"),
		COLON = kw("COLON", ":"),
		THREEDOTS = kw("THREEDOTS", "..."),
		QUESTION = kw("QUESTION", "?"),
		PLUS = kw("PLUS", "+"),
		STAR = kw("STAR", "*"),
		ARROW = kw("ARROW", "->"),
		CHAR = re("CHAR", "'([^']|\\\\(.|[0-9]{3}|x\\p{XDigit}{2}))'"),
		STRING = re("STRING", "\"([^\"]|\\\")*\""),
		LESS = kw("LESS", "<"),
		OCAML_TYPE = re("OCAML_TYPE", "<([^>]|->)*>"),
		OCAML_CODE = new OcamlCodeConsumer(),
		PATTERN = OCAML_CODE,
		ML_COMMENT = re("ML_COMMENT", "\\s*/\\*.*?\\*/\\s*"),
		BAR = kw("BAR", "|"),
		BANG = kw("BANG", "!"),
		EQUAL = kw("EQUAL", "="),
		UPDATE_LOC = re("UPDATE_LOC", "#[ \t]*[0-9]+[ \t]*(\"[^\"\r\n]*\"[ \t]*)?[^\n]*\n")
		;
	
		
	Priority 
		p1 = new Priority("p1"),
		p2 = new Priority("p2", p1),
		pSeq = new Priority("pSeq", p1, p2),
		pAlt = new Priority("pAlt", p1, p2, pSeq);
		
	
	Symbol 
		parser_begin = nt("parser_begin", rule(PERCENTPERCENT), rule(KWD_PARSER)),
		
		lident_list = oneOrMore(LIDENT),
		uident_list = oneOrMore(UIDENT),
		ident_list = oneOrMore(IDENT),
		
		optional_code = opt(OCAML_CODE),
		optional_type = opt(OCAML_TYPE),
		optional_mli = opt(KWD_MLI, OCAML_CODE),
		optional_mlimid = opt(KWD_MLIMID, OCAML_CODE),
		optional_mlitop = opt(KWD_MLITOP, OCAML_CODE),
		optional_mltop = opt(KWD_MLTOP, OCAML_CODE),
		
		token_list = nt("token_list", KWD_TOKEN, oneOrMore(opt(OCAML_TYPE), UIDENT)),
		
		opt_bang = opt(BANG),
		opt_dash = opt(DASH),
		priority = opt(LIDENT),
		action_prio = opt(OCAML_CODE, priority),
		
		symb = nt("symb",
				rule(LIDENT, optional_code, LPAREN, EQUAL, LIDENT, RPAREN),
				rule(LIDENT, optional_code, LPARENLESS, EQUAL, LIDENT, RPAREN),
				rule(LIDENT, optional_code, LPARENLESS, LIDENT, RPAREN),
				rule(LIDENT, optional_code, LPARENGREATER, EQUAL, LIDENT, RPAREN),
				rule(LIDENT, optional_code, LPARENGREATER, LIDENT, RPAREN),
				rule(LIDENT, optional_code),
				rule(UIDENT),
				rule(DASH, LIDENT, optional_code),
				rule(DASH, UIDENT),
				rule(LBRACK, ref("rhs_list"), RBRACK)),

		opt_pattern = opt(PATTERN),
		
		symbol = nt("symbol", symb, optOneOf(STAR, PLUS, QUESTION)),
		symbol_list = zeroOrMore(
			rule(opt(DASH), ref("regexp_ter"), opt_pattern),
			rule(THREEDOTS, OCAML_CODE, opt_pattern),
			rule(symbol, opt_pattern)),
			
		rhs = nt("rhs", opt_bang, symbol_list, opt_dash, action_prio),
		
		rhs_list = nt("rhs_list", oneOrMoreSeparatedBy(rhs, BAR)),
		
		entry_def = nt("entry_def", LIDENT, opt_pattern, COLON, opt(BAR), rhs_list),
	
		grammar = zeroOrMore("grammar", entry_def, opt(SEMI)),
		
		relation = nt("relation", 
				KWD_RELATION, 
				oneOrMore(opt(LESS), LIDENT)),
				
		_regexp = ref("regexp"),
				
		parser_param_info = nt("parser_param_info",
				rule(),
				rule(KWD_START, optional_type, LIDENT),
				rule(token_list),
				rule(relation),
				rule(KWD_CONSTRUCTOR, UIDENT, KWD_FOR, ident_list),
				rule(KWD_CONSTRUCTOR, uident_list),
				rule(KWD_MERGE, LIDENT, uident_list),
				rule(KWD_TYPE, OCAML_TYPE, lident_list),
				rule(KWD_LAYOUT, _regexp, optional_code),
				rule(KWD_NON_TERMINAL, lident_list)),
				
		optional_trailer = nt("optional_trailer",
				rule(),
				rule(PERCENTPERCENT, OCAML_CODE)),
				
		parser_param_infos = oneOrMore(parser_param_info),
		
		char_elt = nt("char_elt", rule(CHAR), rule(CHAR, DASH, CHAR)),
		
		regexp = nt("regexp",
				rule(p1, CHAR),
				rule(p1, LBRACK, oneOrMore(char_elt), RBRACK),
				rule(p1, LBRACK, CARET, oneOrMore(char_elt), RBRACK),
				rule(p1, STRING),
				rule(pAlt, _regexp, BAR, _regexp.lt(pAlt)),
				rule(pSeq, _regexp.le(pSeq), _regexp.lt(pSeq)),
				rule(p2, _regexp.eq(p1), STAR),
				rule(p2, _regexp.eq(p1), PLUS),
				rule(p2, _regexp.eq(p1), QUESTION),
				rule(p1, LIDENT),
				rule(p1, LPAREN, _regexp, RPAREN)
				),
				
		regexp_ter = regexp.lt(pSeq), 
				
		regexp_decl = nt("regexp_decl", LET, LIDENT, EQUAL, regexp),
		
		aux_lexer_def = nt("aux_lexer_def", opt(BAR), nestedRule(regexp, OCAML_CODE), zeroOrMore(BAR, regexp, OCAML_CODE)),
		aux_lexer_and = nt("aux_lexer_and", kw("and"), oneOrMore(LIDENT), EQUAL, kw("parse"), aux_lexer_def),
		
		aux_lexer_rule = nt("aux_lexer_rule", kw("rule"), oneOrMore(LIDENT), EQUAL, kw("parse"), aux_lexer_def),
		
		aux_lexer = nt("aux_lexer", aux_lexer_rule, zeroOrMore(aux_lexer_and)),
				
		opt_token_name = opt(UIDENT),
		
		lexer_rule = nt("lexer_rule", regexp, ARROW, opt_token_name, optional_code),
		
		main_lexer = nt("main_lexer", kw("main"), kw("lexer"), EQUAL, zeroOrMore(lexer_rule)),
		lexer = nt("lexer", KWD_LEXER, 
				zeroOrMore(regexp_decl),
				opt(nestedRule(opt(aux_lexer), main_lexer)));
		
		
	// main: optional_mltop optional_code parser_param_infos lexer ? parser_begin grammar optional_trailer optional_mlitop optional_mlimid optional_mli EOF
		NonTerminal main = nt("main", 
			rule(optional_mltop, optional_code, parser_param_infos, opt(lexer), parser_begin, 
					grammar, optional_trailer, optional_mlitop, optional_mlimid, optional_mli))
					
		;
	
}
