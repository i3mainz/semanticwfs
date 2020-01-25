package de.hsmainz.cs.semgis.wfs.util;

/**
 * Tuple util class.
 * User: Timo Homburg
 * Date: 19.11.13
 * Time: 22:15
 * To change this template use File | Settings | File Templates.
 */
public class Tuple<T,T2> implements Comparable{
    T one;
    T2 two;;
	
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

    public T getOne(){
        return one;
    }

    public void setOne(final T one) {
        this.one = one;
    }

    public T2 getTwo(){
        return two;
    }

    public void setTwo(final T2 two) {
        this.two = two;
    }

    @Override
    public String toString() {
        return "["+this.one+","+this.two+"]";
    }

}
