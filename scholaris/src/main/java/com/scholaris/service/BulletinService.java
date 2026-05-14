package com.scholaris.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.scholaris.dao.BulletinDAO;
import com.scholaris.dao.EtudiantDAO;
import com.scholaris.dao.RangDAO;
import com.scholaris.dao.MatiereDAO;
import com.scholaris.model.Bulletin;
import com.scholaris.model.Etudiant;
import com.scholaris.model.Rang;
import com.scholaris.model.Matiere;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des bulletins et génération PDF via iText.
 */
public class BulletinService {

    private BulletinDAO    bulletinDAO;
    private EtudiantDAO    etudiantDAO;
    private MoyenneService moyenneService;
    private RangService    rangService;
    private RangDAO        rangDAO;
    private MatiereDAO     matiereDAO;

    public BulletinService() throws SQLException {
        this.bulletinDAO    = new BulletinDAO();
        this.etudiantDAO    = new EtudiantDAO();
        this.moyenneService = new MoyenneService();
        this.rangService    = new RangService();
        this.rangDAO        = new RangDAO();
        this.matiereDAO     = new MatiereDAO();
    }

    /**
     * Génère un bulletin complet (moyenne, rang, appréciation, PDF).
     */
    public Bulletin genererEtSauvegarderBulletin(int etudiantId, String periode, int annee) throws SQLException, IOException {
        Etudiant e = etudiantDAO.trouverParId(etudiantId);
        if (e == null) return null;

        double moyGen = moyenneService.calculerMoyenneGenerale(etudiantId, periode, annee);
        Rang r = rangDAO.trouverRangGeneral(etudiantId, periode, annee);
        int rangVal = (r != null) ? r.getRang() : 0;

        Bulletin b = bulletinDAO.trouverParEtudiantEtPeriode(etudiantId, periode, annee);
        if (b == null) {
            b = new Bulletin();
            b.setEtudiant(e);
            b.setClasse(e.getClasse());
            b.setPeriode(periode);
            b.setAnneeScolaire(annee);
        }

        b.setMoyenneGenerale(moyGen);
        b.setRang(rangVal);
        b.setAppreciation(moyenneService.getMention(moyGen));
        
        // Chemin du PDF
        String fileName = "Bulletin_" + e.getNom() + "_" + periode + "_" + annee + ".pdf";
        String filePath = "target/bulletins/" + fileName;
        new File("target/bulletins/").mkdirs();
        
        genererPdf(b, r, filePath);
        b.setFichierPdf(filePath);

        if (b.getId() == 0) bulletinDAO.ajouter(b);
        else bulletinDAO.modifier(b);

        return b;
    }

    /**
     * Génération du fichier PDF avec iText.
     */
    private void genererPdf(Bulletin b, Rang r, String path) throws IOException, SQLException {
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(20, 20, 20, 20);

        // EN-TÊTE AVEC LOGO
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 60, 20})).useAllAvailableWidth();
        
        try {
            URL logoUrl = getClass().getResource("/images/Logo.png");
            if (logoUrl != null) {
                Image logo = new Image(ImageDataFactory.create(logoUrl));
                logo.setWidth(60);
                headerTable.addCell(new Cell().add(logo).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            } else {
                headerTable.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            }
        } catch (Exception e) {
            headerTable.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        }

        Paragraph titlePara = new Paragraph();
        titlePara.add(new Paragraph("IP NET INSTITUTE OF TECHNOLOGY").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        titlePara.add(new Paragraph("\nRÉPUBLIQUE TOGOLAISE - TRAVAIL LIBERTÉ PATRIE").setFontSize(8).setTextAlignment(TextAlignment.CENTER));
        titlePara.add(new Paragraph("\nBULLETIN DE NOTES").setBold().setFontSize(18).setFontColor(ColorConstants.DARK_GRAY).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));
        headerTable.addCell(new Cell().add(titlePara).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        headerTable.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        
        document.add(headerTable);
        document.add(new Paragraph("\n"));

        // INFOS ÉTUDIANT
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        infoTable.addCell(new Cell().add(new Paragraph("Nom : " + b.getEtudiant().getNomComplet()).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        infoTable.addCell(new Cell().add(new Paragraph("Classe : " + b.getClasse().getNom()).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        infoTable.addCell(new Cell().add(new Paragraph("Matricule : " + b.getEtudiant().getMatricule())).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        infoTable.addCell(new Cell().add(new Paragraph("Période : " + b.getPeriode())).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        infoTable.addCell(new Cell().add(new Paragraph("Année Scolaire : " + b.getAnneeScolaire() + "-" + (b.getAnneeScolaire()+1))).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));

        // TABLEAU DES NOTES
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 15, 20, 25})).useAllAvailableWidth();
        table.addHeaderCell(new Cell().add(new Paragraph("MATIÈRE")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addHeaderCell(new Cell().add(new Paragraph("COEF.")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("MOYENNE / 20")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("APPRÉCIATION")).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));

        Map<Integer, Double> moyennes = moyenneService.calculerMoyennesToutesMatieres(b.getEtudiant().getId(), b.getPeriode(), b.getAnneeScolaire());
        
        for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
            Matiere m = matiereDAO.trouverParId(entry.getKey());
            table.addCell(new Cell().add(new Paragraph(m.getNom())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(m.getCoefficient()))).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", entry.getValue()))).setBold().setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(moyenneService.getMention(entry.getValue()))).setTextAlignment(TextAlignment.CENTER));
        }
        document.add(table);

        document.add(new Paragraph("\n"));

        // RÉSUMÉ FINAL
        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
        footerTable.addCell(new Cell().add(new Paragraph("MOYENNE GÉNÉRALE : " + String.format("%.2f", b.getMoyenneGenerale()) + " / 20").setBold().setFontSize(14)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("RANG : " + (b.getRang() > 0 ? b.getRang() + " / " + (r != null ? r.getEffectif() : "?") : "-")).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("APPRÉCIATION : " + b.getAppreciation()).setItalic()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("Signature Direction")).setTextAlignment(TextAlignment.CENTER).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setMarginTop(20));

        document.add(footerTable);

        document.close();
    }
}
