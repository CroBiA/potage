/* 
 * Copyright 2016 University of Adelaide.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package poplogic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Radoslaw Suchecki <radoslaw.suchecki@adelaide.edu.au>
 */
public class Annotation implements Serializable {

    private String geneId;
    private String hitId;
    private String evalue; // AHRD-Quality-Code if annotation is directly from HCS not from rice hits
    private String annotationString;
    private String interpro;

    public Annotation(String gene, HashMap<String, String[]> mipsIdToAnnotationStringToksMap, boolean isRice) {
        String annotationToks[] = null;
        String key = null;
        if (gene.endsWith("*")) {
            key = gene.substring(0, gene.length() - 1);
        } else {
            key = gene;
        }
        geneId = gene;
        annotationToks = mipsIdToAnnotationStringToksMap.get(key);
        if (annotationToks != null && (!isRice || annotationToks.length > 3)) {
            int i = 1;
            try {
                if (isRice) {
                    i += 2;
                }
                hitId = annotationToks[i++];
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                evalue = annotationToks[i++];
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                annotationString = annotationToks[i++];
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                interpro = annotationToks[i++];
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        } else {
//            System.err.println("no annotation!");
            annotationString = "nothing found in rice";

        }
    }
    
    public Annotation(String gene, ArrayList<String> annotationStrings, boolean isRice) {                
        geneId = gene;
        
        if (annotationStrings != null && (!isRice || annotationStrings.size() > 3)) {
            int i = 1;
            try {
                if (isRice) {
                    i += 2;
                }
                hitId = annotationStrings.get(i++);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                evalue = annotationStrings.get(i++);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                annotationString = annotationStrings.get(i++);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            try {
                interpro = annotationStrings.get(i++);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        } else {
//            System.err.println("no annotation!");
            annotationString = "nothing found in rice";

        }
    }

    public String getGeneId() {
        return geneId;
    }

    public String getHitId() {
        return hitId;
    }

    public String getEvalue() {
        return evalue;
    }

    public Double getEvalueDouble() {
        return Double.valueOf(evalue);
    }

    public String getAnnotationString() {
        return annotationString;
    }

    public String getInterpro() {
        return interpro;
    }

    public String getRiceHTML(String fontColour) {
        if (hitId != null) {
            StringBuilder html = new StringBuilder();
            if (hitId.startsWith("LOC")) {
                html.append("<a href=\"http://rice.plantbiology.msu.edu/cgi-bin/ORF_infopage.cgi?db=osa1r6&orf=");
                html.append(hitId);
                html.append("\" value=\"");
                html.append(hitId);
                html.append("\" target=\"_blank\" style=\"color: ").append(fontColour).append("\">");
                html.append(hitId).append("</a> ");
                return html.toString();
            } else {
                return hitId;
            }
        }
        return null;
    }

    public String getMipsHTML(String fontColour) {
        if (hitId != null) {
            StringBuilder html = new StringBuilder();
            boolean isLink = true;
            if (hitId.startsWith("AT")) {
                html.append("<a href=\"http://www.arabidopsis.org/servlets/TairObject?type=locus&name=");
                html.append(hitId.replaceAll("\\.\\d", ""));
            } else if (hitId.startsWith("sp|")) {
                html.append("<a href=\"http://www.uniprot.org/uniprot/");
                html.append(hitId.split("\\|")[1]);
            } else if (hitId.startsWith("UniRef")) {
                html.append("<a href=\"http://www.uniprot.org/uniref/");
                html.append(hitId);
            } else {
                isLink = false;
                html.append("<font color=\"").append(fontColour).append("\">").append(hitId).append("</font> ");
            }

            //if AT/sp/uniprot
            if (isLink) {
                html.append("\" value=\"");
                html.append(hitId);
                html.append("\" target=\"_blank\" style=\"color: ").append(fontColour).append("\">");
                html.append(hitId).append("</a> ");
            }
            return html.toString();
        }
        return null;
    }

    public String getInterproHTML(String fontColour) {
        if (interpro != null) {
            StringBuilder html = new StringBuilder();
            String[] splits = interpro.split(" ");
            html.append("<font color=\"").append(fontColour).append("\">");
            for (String string : splits) {
                if (string.startsWith("IPR")) {
                    html.append("<a href=\"http://www.ebi.ac.uk/interpro/entry/");
                    html.append(string);
                    html.append("\" value=\"");
                    html.append(string);
                    html.append("\" target=\"_blank\" style=\"color: ").append(fontColour).append("\">");
                    html.append(string).append("</a> ");
                } else {
                    html.append(string).append(" ");
//                    html.append("<font color=\"").append(fontColour).append("\">").append(string).append("</font> ");
                }
            }
            html.append("</font> ");
            return html.toString();
        }
        return null;
    }

    public boolean hasInterproID() {
        if (interpro != null) {
            return true;
        }
        return false;
    }
}
