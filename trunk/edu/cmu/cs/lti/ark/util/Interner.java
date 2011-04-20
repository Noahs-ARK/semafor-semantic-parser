package edu.cmu.cs.lti.ark.util;

import java.util.*;

/**
 * For interning (canonicalizing) things.
 * <p/>
 * It maps any object to a unique interned version which .equals the
 * presented object.  If presented with a new object which has no
 * previous interned version, the presented object becomes the
 * interned version.  You can tell if your object has been chosen as
 * the new unique representative by checking whether o == intern(o).
 * The interners use WeakHashMap, meaning that if the only pointers
 * to an interned item are the interners' backing maps, that item can
 * still be garbage collected.  Since the gc thread can silently
 * remove things from the backing map, there's no public way to get
 * the backing map, but feel free to add one at your own risk.
 * <p/>
 * Note that in general it is just as good or better to use the
 * static Interner.globalIntern() method rather than making an
 * instance of Interner and using the instance-level intern().
 * <p/>
 * Author: Dan Klein
 * Date: 9/28/03
 *
 * @author Dan Klein
 */
public class Interner<T> {

  protected static Interner interner = new Interner();

  /**
   * For getting the instance that global methods use.
   */
  public static Interner getGlobal() {
    return interner;
  }

  /**
   * For supplying a new instance for the global methods to
   * use. Returns the previous global interner.
   */
  public static Interner setGlobal(Interner interner) {
    Interner oldInterner = Interner.interner;
    Interner.interner = interner;
    return oldInterner;
  }

  /**
   * Returns a unique object o' that .equals the argument o.  If o
   * itself is returned, this is the first request for an object
   * .equals to o.
   */
  public static Object globalIntern(Object o) {
    return getGlobal().intern(o);
  }


  protected Map<T,T> map = new WeakHashMap<T,T>();

  public void clear() { map = new WeakHashMap<T,T>(); }
  
  /**
   * Returns a unique object o' that .equals the argument o.  If o
   * itself is returned, this is the first request for an object
   * .equals to o.
   */
  public T intern(T o) {
    T i = map.get(o);
    if (i == null) {
      i = o;
      map.put(i, i);
    }
//    else {
//      System.err.println("Found dup for " + o);
//    }
    return i;
  }

  /**
   * Returns a unique object o' that .equals the argument o.  If o
   * itself is returned, this is the first request for an object
   * .equals to o.
   */
  public Set<T> internAll(Set<T> s) {
    Set<T> result = new HashSet<T>();
    for (T o : s) {
      result.add(intern(o));
    }
    return result;
  }

  public int size() {
    return map.size();
  }

  /**
   * Test method: interns its arguments and says whether they == themselves.
   */
  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String str = args[i];
      System.out.println(Interner.globalIntern(str) == str);
    }
  }

}
