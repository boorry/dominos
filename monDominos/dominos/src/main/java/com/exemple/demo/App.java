package com.exemple.demo;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "\t ****************************************  - BEGIN FIRST DOMINOS APP -   ****************************************" );

        JeuDomino jeu = new JeuDomino();
        jeu.initialiser();
        jeu.jouer();

        //listerDominos();


        System.out.println( "\t ****************************************  - END DOMINOS APP -   *************************************************" );
    }

}
