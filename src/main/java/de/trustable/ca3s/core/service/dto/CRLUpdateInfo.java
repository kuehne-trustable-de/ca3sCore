package de.trustable.ca3s.core.service.dto;

public class CRLUpdateInfo {

    boolean bCRLDownloadSuccess = false;
    int crlUrlCount = 0;

    public CRLUpdateInfo(){}

    public void setSuccess(){ bCRLDownloadSuccess = true;}

    public void incUrlCount(){ crlUrlCount++;}

    public boolean isbCRLDownloadSuccess() {
        return bCRLDownloadSuccess;
    }

    public int getCrlUrlCount() {
        return crlUrlCount;
    }
}
