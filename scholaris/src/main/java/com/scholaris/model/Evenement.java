package com.scholaris.model;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class Evenement {
    private int id;
    private String titre;
    private int typeEvalId;
    private TypeEvaluation typeEvaluation;
    private int matiereId;
    private Matiere matiere;
    private int classeId;
    private Classe classe;
    private LocalDate dateEvent;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String description;
    private int creePar;              // utilisateur_id du créateur
    private LocalDateTime creeLe;


    // Constructeurs
    public Evenement() { this.creeLe = LocalDateTime.now(); }

    public Evenement(int id, String titre, int matiereId, int classeId, LocalDate dateEvent, LocalTime heureDebut, LocalTime heureFin) {
        this.id = id; this.titre = titre; this.matiereId = matiereId;
        this.classeId = classeId; this.dateEvent = dateEvent;
        this.heureDebut = heureDebut; this.heureFin = heureFin;
        this.creeLe = LocalDateTime.now();
    }


    // getters/setters

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getTypeEvalId() {
        return typeEvalId;
    }

    public TypeEvaluation getTypeEvaluation() {
        return typeEvaluation;
    }


    public int getMatiereId() {
        return matiereId;
    }


    public Matiere getMatiere() {
        return matiere;
    }


    public int getClasseId() {
        return classeId;
    }

    public Classe getClasse() {
        return classe;
    }

    public LocalDate getDateEvent() {
        return dateEvent;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public String getDescription() {
        return description;
    }

    public int getCreePar() {
        return creePar;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public void setTitre(String t) {
        this.titre = t;
    }

    public void setTypeEvalId(int t) {
        this.typeEvalId = t;
    }

    public void setTypeEvaluation(TypeEvaluation t) {
        this.typeEvaluation = t;
    }
    public void setMatiereId(int m) {
        this.matiereId = m;
    }
    public void setMatiere(Matiere m) {
        this.matiere = m;
    }

    public void setClasseId(int c) {
        this.classeId = c;
    }
    public void setClasse(Classe c) {
        this.classe = c;
    }

    public void setDateEvent(LocalDate d) {
        this.dateEvent = d;
    }

    public void setHeureDebut(LocalTime h) {
        this.heureDebut = h;
    }

    public void setHeureFin(LocalTime h) {
        this.heureFin = h;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public void setCreePar(int c) {
        this.creePar = c;
    }

    public void setCreeLe(LocalDateTime c) {
        this.creeLe = c;
    }


    @Override public String toString() { return "Evenement{titre='" + titre + "', date=" + dateEvent + ", matiere=" + matiereId + "}"; }


    // Comportement de la classe

    public boolean estAVenir() {
        return dateEvent != null && dateEvent.isAfter(LocalDate.now());
    }

    public long getDureeMinutes() {
        if (heureDebut == null || heureFin == null) return 0;
        return java.time.Duration.between(heureDebut, heureFin).toMinutes();
    }

}


