package com.exemple.demo;
public class Payments {
    private StrategiePayement strategie ;

    public void  payementChoisie(){
        if(strategie != null){
            strategie.afficherStrategie();
        }
        else{
            System.out.println("Acun type de payement choisie");
        }
    }

    public void setStrategie(StrategiePayement strategie){
        this.strategie = strategie;
        payementChoisie();
    }
    public Payments(){}
}
