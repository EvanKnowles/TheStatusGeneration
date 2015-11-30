package za.co.knonchalant.status;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

/**
 * Created by evan on 15/02/22.
 */
@ManagedBean
@SessionScoped
public class GenerationBean implements Serializable {

    @ManagedProperty("#{generationAction}")
    GenerationAction generationAction;

    String status;
    private int sanity = 2;
    private Boolean optedIn = null;

    public Boolean getOptedIn() {
        if (optedIn == null) {
            optedIn = generationAction.isUserOptedIn();
        }
        return optedIn;
    }

    public void setOptedIn(Boolean optIn) {
        this.optedIn = optIn;
        generationAction.updateOptedIn(optIn);
    }

    public void generateNewStatus() {
        this.status = generationAction.generateStatus(sanity);
    }

    public String getStatus() {
        if (status == null) {
            status = generationAction.generateStatus(sanity);
        }

        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSanity(int sanity) {
        if (sanity >= 1 && sanity <= 3) {
            this.sanity = sanity;
        }
    }

    public int getSanity() {
        return sanity;
    }

    public void setGenerationAction(GenerationAction generationAction) {
        this.generationAction = generationAction;
    }
}
