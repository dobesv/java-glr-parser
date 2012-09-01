package felix.parser.glr.grammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Define a priority; this makes it easier to handle operator precedence.
 * 
 * Note that a priority is not transitive - that is, specifying that another
 * priority is lower than this one doesn't mean that all the priorities specified
 * as lower than that one will also be considered lower than this one.
 * @author dobes
 *
 */
public class Priority implements Comparable<Priority> {
	public static final Priority DEFAULT = new Priority("default_priority");
	
	public final String id;
	public final List<Priority> lower;
	public Priority(String id, Priority ... lowerPriorities) {
		super();
		this.id = id;
		this.lower = Arrays.asList(lowerPriorities);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Priority other = (Priority) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id;
	}

	public boolean greaterThan(Priority o) {
		return lower.contains(o);
	}
	
	public boolean greaterThanOrEqualTo(Priority o) {
		return !lessThan(o);
	}
	
	public boolean lessThan(Priority o) {
		return o.greaterThan(this);
	}
	
	public boolean lessThanOrEqualTo(Priority o) {
		return !greaterThan(o);
	}
	
	
	@Override
	public int compareTo(Priority o) {
		if(lower.contains(o))
			return -1;
		if(o.lower.contains(this))
			return 1;
		if(equals(o))
			return 0;
		return id.compareTo(o.id);
	}
	
	public abstract class Requirement {
		public abstract boolean check(Priority o);
		public abstract String op();
		@Override
		public String toString() {
			return op()+id;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id.hashCode();
			result = prime * result + op().hashCode();
			return result;
		}
		
		public Priority getPriority() {
			return Priority.this;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj == null || !(obj.getClass().equals(getClass())))
				return false;
			Requirement x = (Requirement)obj;
			return getPriority().equals(x.getPriority()) && op().equals(x.op());
		}
	}
	
	public Requirement requireLessThan() { return new RequireLessThan(); }
	public class RequireLessThan extends Requirement { 
		public boolean check(Priority o) { return o.lessThan(Priority.this); }
		public String op() { return "<"; }
	};
	public Requirement requireLessThanOrEqualTo() { return new RequireLessThanOrEqualTo(); }
	public class RequireLessThanOrEqualTo extends Requirement { 
		public boolean check(Priority o) { return o.lessThanOrEqualTo(Priority.this); } 
		public String op() { return "<="; }
	};
	public Requirement requireGreaterThan() { return new RequireGreaterThan(); }
	public class RequireGreaterThan extends Requirement { 
		public boolean check(Priority o) { return o.greaterThan(Priority.this); }
		public String op() { return ">"; }
	};
	public Requirement requireGreaterThanOrEqualTo() { return new RequireGreaterThanOrEqualTo(); }
	public class RequireGreaterThanOrEqualTo extends Requirement { 
		public boolean check(Priority o) { return o.greaterThanOrEqualTo(Priority.this); } 
		public String op() { return ">="; }
	};
	public Requirement requireEqualTo() { return new RequireEqualTo(); }
	public class RequireEqualTo extends Requirement { 
		public boolean check(Priority o) { return o.equals(Priority.this); } 
		public String op() { return "="; }
	};
	
}
