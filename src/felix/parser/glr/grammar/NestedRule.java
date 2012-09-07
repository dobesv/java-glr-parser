package felix.parser.glr.grammar;


public class NestedRule extends NonTerminal {
	
	static String makeId(Rule ... rules) {
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		for(Rule r : rules) {
			if(buf.length() > 1) buf.append(" | ");
			buf.append(r);
		}
		buf.append(')');
		return buf.toString();
	}
	public NestedRule(Rule ... rules) {
		super(makeId(rules), rules);
	}
	public NestedRule(Symbol ... parts) {
		this(new Rule(parts));
	}
}
