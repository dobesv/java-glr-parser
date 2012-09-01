package felix.parser.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class ReaderCharSequence implements CharSequence {
	static final int CHUNK = 100; 
	private final Reader reader;
	private final int length;
	private final int chunkSize;
	private CharBuffer buf;

	public ReaderCharSequence(Reader reader, int length, int chunkSize) {
		if(chunkSize <= 0) throw new IllegalArgumentException();
		if(reader == null) throw new NullPointerException();
		if(length < 0) throw new IllegalArgumentException();
		this.reader = reader;
		this.length = length;
		this.chunkSize = chunkSize;
	}
	
	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		ensureBuffered(index);
		return buf.charAt(index);
	}

	class SubSequence implements CharSequence {
		final int start;
		final int end;
		
		public SubSequence(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}
		private int translate(int index) {
			int xlated = start+index;
			if(xlated > end) throw new IndexOutOfBoundsException();
			return xlated;
		}
		@Override
		public char charAt(int index) {
			return ReaderCharSequence.this.charAt(translate(index));
		}
		@Override
		public int length() {
			return end-start;
		}
		@Override
		public CharSequence subSequence(int start, int end) {
			return ReaderCharSequence.this.subSequence(translate(start), translate(end));
		}
		
		@Override
		public String toString() {
			return new String(buf.array(), translate(start), translate(end));
		}
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		if(end < start) throw new IllegalArgumentException();
		return new SubSequence(start, end);
	}

	private void ensureBuffered(int index) {
		// Ensure a valid index
		if(index > length) throw new IndexOutOfBoundsException();
		
		// Extend buffer if necessary
		if(buf == null || index > buf.length()) {
			int newlen = Math.min(length, ((index/chunkSize) + 1) * chunkSize);
			CharBuffer newBuffer = CharBuffer.allocate(newlen);
			if(buf != null) newBuffer.put(buf);
			try { reader.read(newBuffer); } catch (IOException e) { throw new Error(e); }// TODO Better exception might be thrown here
			newBuffer.flip();
			buf = newBuffer;
		}
	}
	
	public String toString() {
		return new String(buf.array());
	}
}
