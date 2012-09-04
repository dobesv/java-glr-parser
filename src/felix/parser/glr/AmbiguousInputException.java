package felix.parser.glr;

import java.text.ParseException;

import felix.parser.glr.parsetree.Node;

public class AmbiguousInputException extends ParseException {
	private static final long serialVersionUID = -1801299653864584345L;
	private final Node[] alternatives;

	public AmbiguousInputException(Node[] alternatives) {
		super("Input can be parsed in "+alternatives.length+" different ways.", alternatives[0].getFileRange().getEndOffset());
		this.alternatives = alternatives;
	}

	public Node[] getAlternatives() {
		return alternatives;
	}
}
