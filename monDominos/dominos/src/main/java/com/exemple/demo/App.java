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

        for(int gauche = 6; gauche >= 0; gauche--){
            for(int droite = gauche; droite >= 0; droite--){
                //System.out.println("[" + gauche + "|" + droite + "]");
            }
        }


        System.out.println( "\t ****************************************  - END DOMINOS APP -   *************************************************" );
    }
}
