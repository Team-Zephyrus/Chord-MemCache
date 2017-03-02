package common;

import java.io.Serializable;
import java.math.BigInteger;


public class Key implements Serializable,Comparable<Key>{

    private static final long serialVersionUID = 6063178737434973963L;

    private byte[] keyValue;

	
	public Key(){}
	
	public Key(byte[] keyValue){
		this.keyValue = keyValue;
	}
	
	public Key(String id){
		Hash.hash(id);
		this.keyValue = Hash.getHashedValue();
	}


	public  BigInteger toBigInt(){ return new BigInteger(1,this.keyValue); }
	
	public  String toHex(){
		StringBuilder sb = new StringBuilder();
		for(byte b: this.keyValue){
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	
	public boolean isBetween(Key first,Key second,ClBoundsComparison closedBound){
        Key max = first.max(second);
        Key min = first.min(second);
		switch(closedBound){
            case NONE: { return (this.compareTo(min)==1 && this.compareTo(max)==-1); }
            case LOWER:{ return (this.compareTo(min)>=0 && this.compareTo(max)==-1); }
            case UPPER:{ return (this.compareTo(min)==1 && this.compareTo(max)<=0); }
            case BOTH:{ return (this.compareTo(min)>=0 && this.compareTo(max)<=0); }
		}
        return false;
	}

    public Key max(Key val){
        return this.toBigInt().equals(this.toBigInt().max(val.toBigInt()))? this : val;
    }

    public Key min(Key val){
        return this.toBigInt().equals(this.toBigInt().min(val.toBigInt()))? this : val;
    }

    @Override
    public int compareTo(Key val) {
        return this.toBigInt().compareTo(val.toBigInt());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (!Key.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Key other = (Key) obj;
        if ((this.keyValue == null) && (other.keyValue != null)) {
            return false;
        }
        return this.toBigInt().equals(other.toBigInt());
    }

    @Override
    public int hashCode(){
        return this.toBigInt().hashCode();
    }
    @Override
    public String toString(){ return ""+this.toBigInt();}

    public enum ClBoundsComparison {
        NONE,
        LOWER,
        UPPER,
        BOTH;
    }



}

