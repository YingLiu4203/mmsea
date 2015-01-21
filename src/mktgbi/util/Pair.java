/**
 * 
 */
package mktgbi.util;

/**
 * A Comparable class with two members and 
 * its compareTo() is defined by second member's compareTo().
 * If second member's compareTo() return 0 (equal), then first 
 * member's compareTo() is used -- this avoids false equal in set
 * operations such as TreeSet.add() or HasMap.add(). 
 * @param <T1> Type of the firm member.
 * @param <T2> Type of the second member
 * @author yingliu
 *
 */
public class Pair <T1 extends Comparable<T1>, T2 extends Comparable<T2>> 
	implements Comparable<Pair <T1, T2 >> {

		private T1 member1;
		private T2 member2;
		
		/**
		 * Class Constructor with two member values.
		 * @param memberOne First memeber of the Pair.
		 * @param memberTwo Second memeber of the Pair.
		 */
		public Pair(T1 memberOne, T2 memberTwo) {
			this.member1 = memberOne;
			this.member2 = memberTwo;
		}
		
		/**
		 * Convert to string in a fromat of "member1: member2".
		 * @see java.lang.Object#toString()
		 */
		@Override public String toString() {
			return (this.member1 + ":" + this.member2);
		}
		
		/**
		 * Overrides Ojbect.ComparTo to compare only the second memeber.
		 * @param other 
		 * @return A value is bigger than 0 if this > other;  
		 * A value equlas to 0 if this.equals(other);
		 * A vlaue less than 0 if this < other.   
		 */
		public int compareTo(Pair<T1, T2> other) {
			// we should also compare the first member, otherwise, we couldn't
			// add it to map such as treeset
			int result = this.member2.compareTo(other.member2);
			if (result == 0) {
				// then compare first
				result = this.member1.compareTo(other.member1);
			}
			return result;
		}
		
		/**
		 * @return first member of pair.
		 */
		public T1 getMember1() {
			return this.member1;
		}
		
		/**
		 * @return second member of pair.
		 */
		public T2 getMember2() {
			return this.member2;
		}
}
