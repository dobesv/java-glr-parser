package felix.parser.glr.parsetree;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import felix.parser.glr.grammar.Symbol;
import felix.parser.util.FileRange;

public class Element extends Node {
	public final Node[] children;
	LinkedList<Token> tokens;
	
	public Element(Symbol symbol, Node ... children) {
		this(symbol, null, children);
	}

	/**
	 * 
	 * @param symbol
	 * @param fileRange If non-null, used as the file range; if null, file range is calculated from the children
	 * @param children
	 */
	public Element(Symbol symbol, FileRange fileRange, Node ... children) {
		super(symbol);
		if(children == null) throw new NullPointerException();
		if(fileRange == null && children.length == 0) throw new IllegalStateException("Must provide fileRange if no children provided");
		
		this.children = children;
		this.fileRange = fileRange;
				
		// TODO Ideally we could just have some kind of flattening / chained list here ...
		tokens = new LinkedList<>();
		for(Node n : children) {
			tokens.addAll(n.getTokens());
		}
		
	}
	
	FileRange fileRange;
	
	@Override
	public FileRange getFileRange() {
		if(fileRange == null) fileRange = calculateRange(children);
		return fileRange;
	}
	
	public static FileRange calculateRange(Node ... nodes) {
		if(nodes.length == 0)
			throw new IllegalStateException(); // Not in any file
		FileRange head = nodes[0].getFileRange();
		FileRange tail = nodes[nodes.length-1].getFileRange();
		return new FileRange(head, tail);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(symbol.id).append("(");
		boolean first = true;
		for(Node n : children) {
			if(first) first = false; else sb.append(", ");
			sb.append(n);
		}
		return sb.append(")").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(children);
		result = prime * result
				+ ((fileRange == null) ? 0 : fileRange.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element other = (Element) obj;
		if (!Arrays.equals(children, other.children))
			return false;
		return true;
	}

	@Override
	public List<Token> getTokens() {
		return tokens;
	}
	
	@Override
	public Node getChild(int position) {
		return children[position];
	}
	
	@Override
	public int getChildCount() {
		return children.length;
	}
	
}
