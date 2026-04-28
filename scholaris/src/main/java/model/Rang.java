package model;

/**
 * Représente le rang d'un étudiant dans sa classe.
 * Si matiere == null  → rang général (basé sur la moyenne générale).
 * Si matiere != null  → rang pour cette matière spécifique.
 */
public class Rang {

    private int      id;
    private Etudiant etudiant;
    private Classe   classe;
    private Matiere  matiere;       // null = rang général
    private String   periode;
    private int      anneeScolaire;
    private int      rang;
    private int      effectif;

    public Rang() {}

    public Rang(Etudiant etudiant, Classe classe,
                String periode, int anneeScolaire,
                int rang, int effectif) {
        this.etudiant = etudiant; this.classe = classe;
        this.matiere = null; this.periode = periode;
        this.anneeScolaire = anneeScolaire;
        this.rang = rang; this.effectif = effectif;
    }

    public Rang(Etudiant etudiant, Classe classe, Matiere matiere,
                String periode, int anneeScolaire, int rang, int effectif) {
        this(etudiant, classe, periode, anneeScolaire, rang, effectif);
        this.matiere = matiere;
    }

    public boolean estRangGeneral()  { return matiere == null; }
    public boolean estPodium()       { return rang <= 3; }

    public String getRangFormate() {
        return rang + (rang == 1 ? "er" : "ème") + " / " + effectif;
    }

    public String getType() {
        return matiere == null ? "Rang général" : "Rang en " + matiere.getNom();
    }

    public int getId() {
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }
    public void setEtudiant(Etudiant e) {
        this.etudiant = e;
    }

    public Classe getClasse(){
        return classe;
    }
    public void setClasse(Classe c) {
        this.classe = c;
    }
    public Matiere getMatiere()  {
        return matiere;
    }
    public void setMatiere(Matiere m) {
        this.matiere = m;
    }
    public String getPeriode(){
        return periode;
    }

    public void setPeriode(String p){
        this.periode = p;
    }
    public int getAnneeScolaire()  {
        return anneeScolaire;
    }
    public void setAnneeScolaire(int a) {
        this.anneeScolaire = a;
    }
    public int getRang() {
        return rang;
    }
    public void setRang(int r) {
        this.rang = r;
    }
    public int getEffectif() {
        return effectif;
    }
    public void setEffectif(int e) {
        this.effectif = e;
    }

    @Override
    public String toString() {
        return "Rang{" + getType() + " : " + getRangFormate()
                + " (" + periode + " " + anneeScolaire + ")}";
    }
}
