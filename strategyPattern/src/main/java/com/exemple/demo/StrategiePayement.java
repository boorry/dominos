package com.exemple.demo;
public interface StrategiePayement {
    public void payer(double montant);
    public void afficherStrategie();
}
// mvn exec:java -Dexec.mainClass="strategyPattern"