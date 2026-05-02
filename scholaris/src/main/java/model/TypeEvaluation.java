package model;

public class TypeEvaluation {
    private int id;
    private String nom;
    private String code;
    private double poids;



    // Constructeurs
    public TypeEvaluation() {}

    public TypeEvaluation(int id, String nom, String code, double poids) {
        this.id = id; this.nom = nom; this.code = code; this.poids = poids;
    }
    // getters/setters

    public double getPoids() {
        return poids;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
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

    public void setPoids(double p) {
        this.poids = p;
    }

    @Override
    public String toString() { return "TypeEvaluation{code='" + code + "', nom='" + nom + "', poids=" + poids + "}"; }

}