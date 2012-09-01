package felix.parser.util;

public class FilePos {
	/** Absolute file position in characters; this is the count of characters coming BEFORE this position */
	public final int offset;
	/** Line number in the file.  This is the count of line feeds coming before this line, plus 1 for this line (the first line is 1) */
	public final int line;
	/** Column number in the line.  This is the count of characters since the last line feed or the start of the file, plus one (this first column is 1) */
	public final int col;
	
	public FilePos(int charsRead, int line, int col) {
		super();
		this.offset = charsRead;
		this.line = line;
		this.col = col;
	}
	
	/**
	 * Create a FilePos for the start of the file.
	 */
	public static FilePos START = new FilePos(0,1,1);
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + offset;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilePos other = (FilePos) obj;
		if (offset != other.offset)
			return false;
		return true;
	}
	public boolean before(FilePos successor) {
		return offset <= successor.offset;
	}
	
	
	@Override
	public String toString() {
		return "line "+line+" col "+col+ " offset "+offset;
	}
	public Object toString(FilePos start) {
		if(start.line == this.line)
			return "col "+col+" offset "+offset;
		else
			return "line "+line+" col "+col+ " offset "+offset;
	}
}
