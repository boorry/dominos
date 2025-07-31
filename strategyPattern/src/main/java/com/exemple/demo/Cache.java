package com.exemple.demo;
public class Cache implements StrategiePayement{

    @Override
    public void payer(double montant){
        System.out.println("Montant du payement: " + montant);
    }

    @Override
    public void afficherStrategie(){
        System.out.println("Vous avez choisie de payer par cache");
    }

    public Cache(){   }
}