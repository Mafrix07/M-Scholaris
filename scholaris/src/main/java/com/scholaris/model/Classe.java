package com.scholaris.model;
import java.util.ArrayList;
import java.util.List;

public class Classe {
    private int id;
    private String nom;
    private String niveau;
    private int anneeScolaire;
    private List<Etudiant> etudiants = new ArrayList<>();


    // Constructeurs

    public Classe() {}

    public Classe(int id, String nom, String niveau, int anneeScolaire) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.anneeScolaire = anneeScolaire;
    }

    // Getters ett Setters

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getNiveau() {
        return niveau;
    }

    public int getAnneeScolaire() {
        return anneeScolaire;
    }

    public List<Etudiant> getEtudiants() {
        return etudiants;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setNom(String n) {
        this.nom = n;
    }
    public void setNiveau(String n) {
        this.niveau = n;
    }
    public void setAnneeScolaire(int a) {
        this.anneeScolaire = a;
    }
    public void setEtudiants(List<Etudiant> e) {
        this.etudiants = e;
    }


    // Comportement de la classe

    public void ajouterEtudiant(Etudiant e) {
        if (e != null) etudiants.add(e);
    }

    public int getEffectif() {
        return etudiants.size();
    }

    @Override
    public String toString() { return nom; }
}


