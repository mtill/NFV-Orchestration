package algorithm;

public class Tuples {
	
	public final static class Tuple<X, Y> {
		public X x;
		public Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
		
		public String toString() {
			return "(" + x + ", " + y + ")";
		}

	}

	public final static class Triple<X, Y, Z> {
		public X x;
		public Y y;
		public Z z;

		public Triple(X x, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

	}
	
	public final static class Quadruple<X, W, Y, Z> {
		public X x;
		public W w;
		public Y y;
		public Z z;

		public Quadruple(X x, W w, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.w = w;
		}

	}
	
	public final static class Quintuple<X, W, Y, Z, A> {
		public X x;
		public W w;
		public Y y;
		public Z z;
		public A a;

		public Quintuple(X x, W w, Y y, Z z, A a) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.w = w;
			this.a = a;
		}

	}

}
