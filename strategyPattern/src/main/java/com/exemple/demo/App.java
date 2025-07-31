package com.exemple.demo;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Payments paie = new Payments();
        paie.setStrategie(new Cache());
        paie.payementChoisie();
        
    }
}
// mvn exec:java -Dexec.mainClass="com.exemple.demo.App"