package com.scholaris.model;

public class Matiere {
    private int id;
    private String nom;
    private String code;
    private double coefficient;


    // Constructeurs

    public Matiere() {}

    public Matiere(int id, String nom, String code, double coefficient) {
        this.id = id;
        this.nom = nom;
        this.code = code;
        this.coefficient = coefficient;
    }


    // Getters et Setters

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getCode() {
        return code;
    }

    public double getCoefficient() {
        return coefficient;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String n) {
        this.nom = n;
    }
    public void setCode(String c) {
        this.code = c;
    }
    public void setCoefficient(double c) {
        this.coefficient = c;
    }

    /** Calcule la note pondérée par le coefficient. */
    public double getPonderee(double note) {
        return note * coefficient;
    }



    @Override public String toString() { return "Matiere{code='" + code + "', nom='" + nom + "', coef=" + coefficient + "}"; }
}