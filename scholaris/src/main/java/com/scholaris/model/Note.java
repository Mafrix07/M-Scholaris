package com.scholaris.model;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private int etudiantId;
    private int matiereId;
    private Matiere matiere;
    private int classeId;
    private int typeEvalId;
    private TypeEvaluation typeEvaluation;
    private int evenementId;
    private Evenement evenement;
    private double valeur;             // 0.0 à 20.0
    private String periode;            // "trimestre1", "semestre1"...
    private int anneeScolaire;
    private int modifiePar;
    private LocalDateTime modifieLe;


    public Note() {
        this.modifieLe = LocalDateTime.now();
    }


    public Note(int id, int etudiantId, int matiereId, int classeId,
                double valeur, String periode, int anneeScolaire) {
        this.id = id; this.etudiantId = etudiantId; this.matiereId = matiereId;
        this.classeId = classeId; this.valeur = valeur;
        this.periode = periode; this.anneeScolaire = anneeScolaire;
        this.modifieLe = LocalDateTime.now();
        valider();
    }













    // Comportement de la classe


    private void valider() {
        if (valeur < 0 || valeur > 20)
            throw new IllegalArgumentException("La note doit être entre 0 et 20. Reçu : " + valeur);
    }


    /** Retourne la note pondérée si la matière est chargée. */
    public double getValeurPonderee() {
        if (matiere == null) return valeur;
        return valeur * matiere.getCoefficient();
    }

    /** Retourne l'appréciation textuelle de la note. */
    public String getAppreciation() {
        if (valeur >= 16) return "Très Bien";
        if (valeur >= 14) return "Bien";
        if (valeur >= 12) return "Assez Bien";
        if (valeur >= 10) return "Passable";
        return "Insuffisant";
    }


    public void setValeur(double v) { this.valeur = v; valider(); }

    public int getId() {
        return id ;
    }

    public void setId(int id)
    {
        this.id = id;
    }


    public int getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(int e) {
        this.etudiantId = e;
    }

    public int getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(int m) {
        this.matiereId = m;
    }

    public Matiere getMatiere() {
        return matiere;
    } public void setMatiere(Matiere m) {
        this.matiere = m;
    }

    public int getClasseId() {
        return classeId;
    }
    public void setClasseId(int c) {
        this.classeId = c;
    }

    public int getTypeEvalId() {
        return typeEvalId;
    }
    public void setTypeEvalId(int t) {
        this.typeEvalId = t;
    }

    public TypeEvaluation getTypeEvaluation() {
        return typeEvaluation;
    }

    public void setTypeEvaluation(TypeEvaluation t) {
        this.typeEvaluation = t;
    }

    public int getEvenementId() {
        return evenementId; }
    public void setEvenementId(int e) {
        this.evenementId = e;
    }
    public Evenement getEvenement() {
        return evenement;
    } public void setEvenement(Evenement e) {
        this.evenement = e;
    }

    public double getValeur() {
        return valeur;
    }

    public String getPeriode() {
        return periode; }
    public void setPeriode(String p) {
        this.periode = p;
    }
    public int getAnneeScolaire() {
        return anneeScolaire;
    }
    public void setAnneeScolaire(int a) {
        this.anneeScolaire = a;
    }

    public int getModifiePar() {
        return modifiePar;
    }
    public void setModifiePar(int m) {
        this.modifiePar = m;
    }
    public LocalDateTime getModifieLe() {
        return modifieLe;
    }
    public void setModifieLe(LocalDateTime m) {
        this.modifieLe = m;
    }

    @Override public String toString() { return "Note{valeur=" + valeur + ", matiere=" + matiereId + ", periode='" + periode + "', apprec='" + getAppreciation() + "'}"; }

}
