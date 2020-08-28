package de.hsmainz.cs.semgis.wfs.util;

/**
 * Utility class modelling a tuple.
 */
public class Tuple<T,T2> implements Comparable{
	/** The first value.*/
    T one;
    /** The second value.*/
    T2 two;
	
    /**
     * Constructor for this class.
     * @param one the first value
     * @param two the second value
     */
    public Tuple(T one, T2 two){
        this.one=one;
        this.two=two;
    }
    
    @Override
    public int compareTo(Object o) {
        Tuple t=(Tuple) o;
        if(t.two== this.two && this.one==t.one)
            return 0;
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tuple)
            return this.one.equals(((Tuple)obj).one) && this.two.equals(((Tuple)obj).two);
        return false;
    }

    /**
     * Gets the first value.
     * @return The first value
     */
    public T getOne(){
        return one;
    }
        
    /**
     * Sets the first value.
     */
    public void setOne(final T one) {
        this.one = one;
    }

    /**
     * Gets the second value.
     * @return The second value
     */
    public T2 getTwo(){
        return two;
    }

    /**
     * Sets the second value.
     */
    public void setTwo(final T2 two) {
        this.two = two;
    }

    @Override
    public String toString() {
        return "["+this.one+","+this.two+"]";
    }

}
