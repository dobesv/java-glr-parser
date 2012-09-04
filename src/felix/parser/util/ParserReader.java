package felix.parser.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import felix.parser.glr.grammar.PatternTerminal;
import felix.parser.glr.grammar.Symbol;
import felix.parser.glr.grammar.Terminal;
import felix.parser.glr.parsetree.Token;

/**
 * A reader that keeps track of the file line and column information.
 * 
 */
public class ParserReader extends Reader {

	static class Pos {
		int line=1;
		int col=1;
		int offset=0;
		
		public Pos() {
		}
		public Pos(Pos other) {
			assign(other);
		}
		public void assign(Pos other) {
			this.line = other.line;
			this.col = other.col;
			this.offset = other.offset;
		}
		public void assign(FilePos filePos) {
			this.line = filePos.line;
			this.col = filePos.col;
			this.offset = filePos.offset;
		}
		
		
		private void accumulate(int ch) {
			if(ch == -1)
				return;
			
			if(ch == '\n') {
				line++;
				col = 1;
			} else {
				col++;
			}
			offset++;
		}
		public FilePos toFilePos() {
			return new FilePos(offset, line, col);
		}
	}
	
	final Reader delegate;
	final String filename;
	
	final Pos current = new Pos();
	final Pos mark = new Pos();
	public final int fileSize; // In chars
	
	public int read(CharBuffer target) throws IOException {
		int offset = target.position();
		int charsRead = delegate.read(target);
		for(int i=0; i < charsRead; i++) {
			accumulate(target.get(offset+i));
		}
		return charsRead;
	}

	public int read() throws IOException {
		int ch = delegate.read();
		accumulate(ch);
		return ch;
	}

	private void accumulate(int ch) {
		current.accumulate(ch);
	}

	public int read(char[] cbuf) throws IOException {
		int len = delegate.read(cbuf);
		for(int i=0; i < len; i++) {
			accumulate(cbuf[i]);
		}
		return len;
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		int lenRead = delegate.read(cbuf, off, len);
		for(int i=0; i < lenRead; i++) {
			accumulate(cbuf[off+i]);
		}
		return lenRead;
	}

	public long skip(long n) throws IOException {
		// We need to keep our line number accurate
		for(long skipped=0; skipped < n; skipped++) {
			if(read() == -1)
				return skipped;
		}
		return n;
	}

	public boolean ready() throws IOException {
		return delegate.ready();
	}

	public boolean markSupported() {
		return true;
	}

	public void mark(int readAheadLimit) throws IOException {
		delegate.mark(readAheadLimit);
		mark.assign(current);
	}

	/**
	 * Reset to the last mark. 
	 */
	public void reset() throws IOException {
		delegate.reset();
		current.assign(mark);
	}

	/**
	 * Reset to a given absolute offset in characters.
	 * <p>
	 * Note that you cannot seek to before the mark, so call mark() only if 
	 * you know you will not seek before that mark.
	 * <p>
	 * However, this will be faster if you do call mark() periodically,
	 * since it will seek back to the last mark and then scan characters
	 * to recalculate the line number at the target position.
	 * 
	 * @throws IndexOutOfBoundsException If the offset provided is before the last mark or beyond the end of the file
	 */
	public void seek(int offset) throws IOException {
		if(offset > fileSize) throw new IndexOutOfBoundsException("Past EOF");
		
		if(offset == current.offset) {
			// Do nothing, we're already there
		} else if(offset > current.offset) {
			// Scan ahead
			skip(offset-current.offset);
		} else if(offset == mark.offset) {
			// Jump back to the mark
			reset();
		} else if(offset > mark.offset) {
			// Save some line/column calculations if we're seeking back within the current line
			int charsBack = current.offset - offset;
			boolean sameLineAsCurrent = charsBack < current.col;
			if(sameLineAsCurrent) {
				// Reposition the reader on the desired character
				delegate.reset();
				delegate.skip(offset-mark.offset);
				
				// Just directly calculate the column number
				current.col -= charsBack;
				current.offset = offset;
			} else {
				// Jump back to the mark, then scan ahead to the given offset while recomputing the file offset
				// This is slower than the above
				reset();
				skip(offset - mark.offset);
			}
		} else {
			throw new IndexOutOfBoundsException("Cannot scan back past the last mark.");
		}
	}
	
	/**
	 * Read the file position from the parameter and seek to that position, if possible.
	 * <p>
	 * Note that this will assume that the line and column information provided are
	 * correct for the given offset.
	 * 
	 * @see #seek(int)
	 */
	public void seek(Pos offset) throws IOException {
		if(offset.offset == current.offset) {
			// Do nothing, we're already there
		} else if(offset.offset == mark.offset) {
			// Jump back to the mark
			reset();
		} else if(offset.offset > current.offset){
			skip(offset.offset - current.offset);
		} else if(offset.offset > mark.offset){
			delegate.reset();
			delegate.skip(offset.offset-mark.offset);
			current.assign(offset);
		} else {
			throw new IndexOutOfBoundsException("Cannot scan back past the last mark.");
		}
	}
	
	/**
	 * Read the file position from the parameter and seek to that position, if possible.
	 * <p>
	 * Note that this will assume that the line and column information provided are
	 * correct for the given offset.  To seek to just an offset, call seek(int).
	 * 
	 * @see #seek(int)
	 */
	public void seek(FilePos filePos) throws IOException {
		if(filePos.offset == current.offset) {
			// Do nothing, we're already there
		} else if(filePos.offset == mark.offset) {
			// Jump back to the mark
			reset();
		} else if(filePos.offset > current.offset) {
			skip(filePos.offset - current.offset);
		} else if(filePos.offset > mark.offset){
			delegate.reset();
			delegate.skip(filePos.offset-mark.offset);
			current.assign(filePos);
		} else {
			throw new IndexOutOfBoundsException("Cannot scan back past the last mark.");
		}
	}
	
	/**
	 * Seek to the end of the given token.  The token is assumed to have come from
	 * this file and to have a correct end line and column in it.
	 * <p>
	 * After this call, the file position will be set to read the character
	 * immediately following thr provided token.
	 * 
	 * @param token Token to use as a reference.
	 * @throws IOException If bytes had to be read from the the file and an errors occurs while doing so
	 */
	public void seekPast(Token token) throws IOException {
		seek(token.getFileRange().getEnd());
	}
	
	/**
	 * Get the current offset into the source, in characters.  This starts at 0.
	 */
	public int getCurrentOffset() {
		return current.offset;
	}

	/**
	 * Get the current line number in the source.  This starts at 1.
	 */
	public int getCurrentLineNumber() {
		return current.line;
	}

	/**
	 * Get the current column number in the source.  This starts at 1.
	 */
	public int getCurrentColumnNumber() {
		return current.col;
	}
	
	public void close() throws IOException {
		delegate.close();
	}

	public int remaining() {
		return fileSize - current.offset;
	}
	
	/**
	 * Create a CharSequence wrapping this reader at its current position.
	 * 
	 * Don't use the Reader and the CharSequence in parallel.  Note that
	 * the CharSequence reads ahead and may advance the reader position arbitrarily;
	 * use mark() and reset() to restore the file position after using 
	 * this feature.
	 */
	public CharSequence toCharSequence() {
		return new ReaderCharSequence(this, remaining(), 1024);
	}
	
	/**
	 * Attempt to match the given regular expression against the next
	 * available characters in the stream.
	 *
	 * Returns a Matcher indicating the result of the match.
	 * 
	 * This method will advance the file pointer arbitrarily; be sure to
	 * use reset() when you are done with the matcher to go to your
	 * desired file position.
	 */
	public Matcher matcher(Pattern p) throws IOException {
		return p.matcher(toCharSequence());
	}
	
	/**
	 * Look ahead for the given regular expression; return a Token
	 * if the pattern matches.
	 *
	 * One should seek to the desired parse position before calling this;
	 * it leaves the file position after the token that was consumed (if any)
	 * or in the original position if the match fails.
	 * This will not match an empty string even if the regular expression
	 * would allow it.
	 * 
	 * @param ignoredTokens Tokens that were ignored as comments/whitespace immediately before this one 
	 */
	public Token checkNextToken(Pattern re, PatternTerminal term, String ignored) throws IOException {
		FilePos start = getFilePos();
		Matcher m = matcher(re);
		if(m.lookingAt() && m.end() > m.start()) {
			// Position just at the end of the token that was matched
			seek(start.offset + m.end());
			return new Token(getFileRange(start), term, m.group(), ignored);
		}
		seek(start);
		return null;
	}
	
	/**
	 * Get the current file position as a FilePos instance.
	 */
	public FilePos getFilePos() {
		return current.toFilePos();
	}

	/**
	 * Create a new parser reader.
	 * 
	 * @param delegate Reader to delegate to.  If it doesn't support marks, this will 
	 *                 wrap it in a buffered reader capable of buffering the entire file.
	 *                 If it does support marks, it should never invalidate the mark
	 *                 no matter how many characters are read.  This calls mark() immediately
	 *                 to allow "seeking" within the stream.
	 * @param filename Name of the file to report in the file location information attached to tokens
	 * @param fileSize Total length of the file
	 * @throws IOException 
	 */
	public ParserReader(Reader delegate, String filename, int fileSize) throws IOException {
		if(!delegate.markSupported()) {
			delegate = new BufferedReader(delegate, fileSize);
		}
		this.delegate = delegate;
		this.filename = filename;
		this.fileSize = fileSize;
		mark();
	}

	public void mark() throws IOException {
		mark(remaining());
	}

	/**
	 * Return the file range from the given position to the current position.
	 */
	public FileRange getFileRange(FilePos from) {
		return new FileRange(filename, from, current.toFilePos());
	}

	public String getFilename() {
		return filename;
	}

	/** Return a zero-length token at the current file position */
	public Token markerToken(Symbol marker) {
		return new Token(getFileRange(getFilePos()), marker, "");
	}

	/**
	 * Read any input matching the given terminals and return it as a string.
	 * <p> 
	 * The input is positioned immediately after the last character matched.
	 */
	public String consume(Set<Terminal> ignore) throws IOException {
		FilePos startStartPos = getFilePos();
		FilePos startPos = startStartPos;
		for(;;) {
			for(Terminal term : ignore) {
				term.match(this, null, null);
			}
			FilePos endPos = getFilePos();
			if(endPos.equals(startPos)) {
				// No forward movement, we're done
				int chars = endPos.offset - startStartPos.offset;
				seek(startStartPos);
				return readString(chars);
			}
			startPos = endPos;
		}
		
	}

	public String readString(int chars) throws IOException {
		char[] buf = new char[chars];
		int remaining = chars;
		while(remaining > 0) {
			int didRead = read(buf);
			if(didRead == -1) throw new EOFException();
			remaining -= didRead;
		}
		return new String(buf); 
	}
}
