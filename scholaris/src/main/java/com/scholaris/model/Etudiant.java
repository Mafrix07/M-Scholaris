package com.scholaris.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Etudiant extends Utilisateur {
    private int utilisateurId;
    private String matricule;
    private LocalDate dateNaissance;
    private int classeId;
    private Classe classe;
    private List<Note> notes = new ArrayList<>();
    private List<Bulletin> bulletins = new ArrayList<>();


    // constructeurs

    public Etudiant() {
        super();
        setRole("etudiant");
    }

    public Etudiant(int id, String nom, String prenom, String email, String motDePasse, String matricule, LocalDate dateNaissance, int classeId) {
        super(id, nom, prenom, email, motDePasse, "etudiant");
        this.matricule = matricule;
        this.dateNaissance = dateNaissance;
        this.classeId = classeId;
    }
    // Getters et Setters
    // 1 Getters
    public int getUtilisateurId() {
        return utilisateurId;
    }

    public String getMatricule() {
        return matricule;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public int getClasseId() {
        return classeId;
    }

    public Classe getClasse() {
        return classe;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Bulletin> getBulletins() {
        return bulletins;
    }

    // 2 Setters
    public void setUtilisateurId(int u) {
        this.utilisateurId = u;
    }

    public void setMatricule(String m) {
        this.matricule = m;
    }

    public void setDateNaissance(LocalDate d) {
        this.dateNaissance = d;
    }

    public void setClasseId(int c) {
        this.classeId = c;
    }

    public void setClasse(Classe c) {
        this.classe = c;
    }

    public void setNotes(List<Note> n) {
        this.notes = n;
    }

    public void setBulletins(List<Bulletin> b) {
        this.bulletins = b;
    }

    // Comportement de classe Etudiant

    public void ajouterNote(Note n) {
        if (n != null) notes.add(n);
    }

    public List<Note> getNotesByMatiere(int matiereId) {
        List<Note> res = new ArrayList<>();
        for (Note n : notes) if (n.getMatiereId() == matiereId) res.add(n);
        return res;
    }

    public double getMoyenneParMatiere(int matiereId) {
        List<Note> lst = getNotesByMatiere(matiereId);
        if (lst.isEmpty()) return 0.0;
        double s = 0; for (Note n : lst) s += n.getValeur();
        return Math.round((s / lst.size()) * 100.0) / 100.0;
    }

    public int getAge() {
        if (dateNaissance == null) return 0;
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }

    // Fonctions communes aux utilisateurs mais redefinies
    @Override
    public String[] getPermissions() {
        return new String[]{"VOIR_NOTES","VOIR_BULLETIN","VOIR_CALENDRIER","TELECHARGER_BULLETIN"};
    }

    @Override
    public void afficherDashboard() {
        System.out.println("=== Dashboard Étudiant : " + getNomComplet() + " ===");
        System.out.println("Matricule : " + matricule);
        System.out.println("Nombre de notes : " + notes.size());
    }
}

