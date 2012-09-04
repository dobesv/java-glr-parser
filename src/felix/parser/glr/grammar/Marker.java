package felix.parser.glr.grammar;

import java.io.IOException;

import felix.parser.glr.Parser.StackHead;
import felix.parser.glr.parsetree.Node;
import felix.parser.util.ParserReader;

public class Marker extends Terminal {
	/**
	 * This marker matches zero characters, but only at the start of the file.
	 */
	public static final Marker START_OF_FILE = new Marker("<start>") {
		public Node match(ParserReader input, StackHead head, String ignored) throws IOException {
			if(input.getCurrentOffset() == 0)
				return input.markerToken(this);
			else
				return null;
		}
	};
	
	/**
	 * This marker matches zero characters, but only at the end of the file.
	 */
	public static final Marker END_OF_FILE = new Marker("<end>") {
		public Node match(ParserReader input, StackHead head, String ignored) throws IOException {
			if(input.getCurrentOffset() == input.fileSize)
				return input.markerToken(this);
			else
				return null;
		}
	};
	
	/**
	 * This marker is always a successful match of zero characters.
	 */
	public static final Marker NIL = new Marker("<nil>") {
		public Node match(ParserReader input, StackHead head, String ignored) throws IOException {
			return input.markerToken(this);
		}
	};

	public Marker(String name) {
		super(name);
	}
	
	@Override
	public Node match(ParserReader input, StackHead head, String ignored) throws IOException {
		return null;
	}
}
