/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poplogic;

import beans.Location_cMFilter;
import beans.MainPopsBean;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author rad
 */
public class PerLocationContigs implements Serializable {

    private final ArrayList<Contig> contigs;
    private ArrayList<Contig> filteredContigs;
    private ArrayList<Contig> selectedContigs;
    private final String chromosome;
    private final Location_cMFilter cM_filterForNonGeneContigs;
    private StreamedContent exportFileWholeIWGSC;
    
    //for contigs without genes - good to have the range of available cM locations for autocomplete
    private final ArrayList<Double> cM_correctedRange;
    private final ArrayList<Double> cM_originalRange;

    public PerLocationContigs(String chromosome, Location_cMFilter cM_filterForNonGeneContigs) {
        this.chromosome = chromosome;
        this.contigs = new ArrayList<>();
        this.filteredContigs = null;
        this.cM_correctedRange = new ArrayList<>();
        this.cM_originalRange = new ArrayList<>();
        this.cM_filterForNonGeneContigs = cM_filterForNonGeneContigs;
    }

//    public PerLocationContigs(String chromosome, double cM_corrected_min, double cM_corrected_max, double cM_original_min, double cM_original_max) {
//        this.chromosome = chromosome;
//        this.cM_corrected_min = cM_corrected_min;
//        this.cM_corrected_max = cM_corrected_max;
//        this.cM_original_min = cM_original_min;
//        this.cM_original_max = cM_original_max;
//        this.contigs = new ArrayList<>();
//        this.filteredContigs = new ArrayList<>();
//        this.cM_correctedRange = new ArrayList<>();
//        this.cM_originalRange = new ArrayList<>();
//        cM_filterForNonGeneContigs = new Location_cMFilter();
//    }
    public void addContig(Contig c) {
        if (contigs.isEmpty()) {
            cM_correctedRange.add(c.getcM_corrected());
            cM_originalRange.add(c.getcM_original());
        } else { //risky way of collecting all cM positions (assumes input is sorted)
            Contig previous = contigs.get(contigs.size() - 1);
            if (previous.getcM_corrected() != c.getcM_corrected()) {
                cM_correctedRange.add(c.getcM_corrected());
            }
            if (previous.getcM_original() != c.getcM_original()) {
                cM_originalRange.add(c.getcM_original());
            }
        }
        contigs.add(c);
    }

    public ArrayList<Contig> getContigs() {
        return contigs;
    }

    public Location_cMFilter getcM_filterForNonGeneContigs() {
        return cM_filterForNonGeneContigs;
    }

    public ArrayList<Contig> getFilteredContigs() {

        return filteredContigs;
    }

    public void setFilteredContigs(ArrayList<Contig> filteredContigs) {
        this.filteredContigs = filteredContigs;
    }

    public ArrayList<Double> correctedRangePrefix(String prefix) {
        ArrayList<Double> corrected = new ArrayList<>();
        for (Double value : cM_correctedRange) {
            if (value.toString().startsWith(prefix)) {
                corrected.add(reusable.CommonMaths.round(value, 3));
            }
        }
        return corrected;
    }

    public ArrayList<Double> originalRangePrefix(String prefix) {
        ArrayList<Double> corrected = new ArrayList<>();
        for (Double value : cM_originalRange) {
            if (value.toString().startsWith(prefix)) {
                corrected.add(reusable.CommonMaths.round(value, 3));
            }
        }
        return corrected;
    }

    public ArrayList<Double> getcM_correctedRange() {
        return cM_correctedRange;
    }

    public ArrayList<Double> getcM_originalRange() {
        return cM_originalRange;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void resetFilter() {
        setFilteredContigs(null);
    }

    public boolean isFiltered() {
        return filteredContigs != null;
    }

    public void filterByCm() {
        filteredContigs = new ArrayList<>();
        if (contigs != null) {
            for (Contig contig : contigs) {
                double cM_corrected = contig.getcM_corrected();
                double cM_original = contig.getcM_original();
                if (cM_filterForNonGeneContigs.isWithinUserCoordinates(cM_corrected, cM_original)) {
                    filteredContigs.add(contig);
                }
            }

            //reset selection to prevent erratic behaviour (e.g. first elem in the table remains selected even though it is a different elem due to cM restriction
            setSelectedContigs(null);
        }
    }

    public ArrayList<Contig> getSelectedContigs() {
        return selectedContigs;
    }

    public void setSelectedContigs(ArrayList<Contig> selectedContigs) {
        this.selectedContigs = selectedContigs;
    }

    public void clearSelectedContigs() {
        setSelectedContigs(null);
    }

    public boolean somethingSelected() {
        return selectedContigs != null && !selectedContigs.isEmpty();
    }

    private void exportWholeIWGSCFile() {
        if (selectedContigs == null || selectedContigs.isEmpty()) {
        } else {
            String newline = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder();
            for (Contig c : selectedContigs) {
                System.err.println("Selected: " + c.getId() + " " + c.getId());
                sb.append(">");
                sb.append(c.getContigId());
//                sb.append(" ").append(p.getHit().getHitDef());
//                sb.append(" predicted POPSEQ location ");
//                sb.append(barleyChromosome);
//                sb.append("H:");
//                sb.append(c.getFrom());
//                sb.append("-").append(c.getTo());
//                sb.append(" ").append(c.getFrame());
                sb.append(newline);
                sb.append(reusable.BlastOps.getCompleteSubjectSequence(c.getContigId(), MainPopsBean.BLAST_DB_FOR_FETCHING).get(0).getSequenceString());
                sb.append(newline);
            }
            InputStream stream = new ByteArrayInputStream(sb.toString().getBytes());
            this.exportFileWholeIWGSC = new DefaultStreamedContent(stream, "application/txt", "Selected_IWGSC_contigs_" + reusable.CommonMaths.getRandomString() + ".fasta");

//            FacesContext context = FacesContext.getCurrentInstance();
//            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Saving...",  selectedContigs.size() + " selected contig(s)"));
//            RequestContext requestContext = RequestContext.getCurrentInstance();
//            requestContext.update("form:dataTable:saveFastaWhole");
//            context.addMessage(null, new FacesMessage("Second Message", "Additional Info Here..."));
//            System.err.println(sb.toString());
        }
    }

    public StreamedContent getExportFileWholeIWGSC() {
        exportWholeIWGSCFile();
        return exportFileWholeIWGSC;
    }
    
    public Integer getIndexOfContig(String contigId) {
        Integer idx = null;
        for (int i = 0; i < contigs.size(); i++) {
            String id = contigs.get(i).getId();
            if(id.equalsIgnoreCase(contigId)){
                idx = i;
                break;
            }
        }
        return idx;
    }
    
}