package za.co.knonchalant.status;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.*;

/**
 * Created by evan on 15/02/28.
 */
@ManagedBean
@SessionScoped
public class GenerationAction {

    private static final int CUT_OFF = 10;
    @ManagedProperty("#{facebookBean}")
    FacebookBean facebookBean;

    @EJB
    StatusDAO statusDAO;

    public void postStatus(String status) {
        facebookBean.postStatus(status);
    }

    public void toggleFriend(SelectedWrapper<StatusUser> friend) {
        friend.setSelected(!friend.isSelected());
    }

    public Boolean isUserOptedIn() {
        StatusUser user = statusDAO.findUserByUserId(facebookBean.getFacebookId());
        if (user != null) {
            return user.isOptInShare();
        }
        return null;
    }

    public String generateStatus(int sanity) {
        Map<String, List<String>> mapStatuses = getStatuses(facebookBean.getStatuses());
        List<String> statuses = mapStatuses.get(facebookBean.getFacebookId());

        if (statuses == null || statuses.isEmpty()) {
            return "";
        }

        List<String> friends = getMashUpList(facebookBean.getOptedInFriends());
        Map<String, List<String>> friendStatuses = getStatuses(statusDAO.findByUsers(friends));

        statuses = combine(statuses, friendStatuses);

        LanguageModel root = new LanguageModel(sanity);
        for (String status : statuses) {
            List<String> words = Arrays.asList(status.split(" "));
            root.addSentence(words);
        }
        root.calculate();
        String newStatus = root.generateSentence();
        int attempts = 0;
        while (statuses.contains(newStatus) && attempts < CUT_OFF) {
            attempts++;
            newStatus = root.generateSentence();
        }
        return newStatus;
    }

    private List<String> combine(List<String> statuses, Map<String, List<String>> friendStatuses) {
        if (friendStatuses.isEmpty()) {
            return statuses;
        }

        int min = statuses.size();
        String person = facebookBean.getFacebookId();

        for (String personKey : friendStatuses.keySet()) {
            int size = friendStatuses.get(personKey).size();
            if (size < min) {
                min = size;
                person = personKey;
            }
        }

        StatusUser user = statusDAO.findUserByUserId(person);

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(user + " only has " + min + " statuses, limiting to that."));

        ArrayList<String> result = getRandom(statuses, min);
        for (String personKey : friendStatuses.keySet()) {
            result.addAll(getRandom(friendStatuses.get(personKey), min));
        }
        return result;
    }

    private ArrayList<String> getRandom(List<String> statuses, int min) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> statusCopy = new ArrayList<>(statuses);

        if (min == statusCopy.size()) {
            return statusCopy;
        }

        Random rand = new Random();

        for (int i = 0; i < min; i++) {
            int index = (int) (rand.nextDouble() * statusCopy.size());
            result.add(statusCopy.remove(index));
        }

        return result;
    }

    private List<String> getMashUpList(List<SelectedWrapper<StatusUser>> optedInFriends) {
        List<StatusUser> users = SelectedWrapper.getSelected(optedInFriends);
        List<String> userIds = new ArrayList<>();
        for (StatusUser user : users) {
            if (statusDAO.userIsOptedIn(user.getFacebookUserId())) {
                userIds.add(user.getFacebookUserId());
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("You're mashing with " + user.getFirstName() + " but they're not mashing anymore."));
                facebookBean.resetFriends();
            }
        }
        return userIds;
    }

    private Map<String, List<String>> getStatuses(List<Status> statuses) {
        Map<String, List<String>> statusStrings = new HashMap<>();
        for (Status post : statuses) {
            String message = post.getText();
            if (message != null) {
                if (!statusStrings.containsKey(post.getUserId())) {
                    statusStrings.put(post.getUserId(), new ArrayList<String>());
                }
                statusStrings.get(post.getUserId()).add(message);
            }
        }
        return statusStrings;
    }

    public void setFacebookBean(FacebookBean facebookBean) {
        this.facebookBean = facebookBean;
    }

    public void updateOptedIn(Boolean optIn) {
        StatusUser user = statusDAO.findUserByUserId(facebookBean.getFacebookId());
        if (user != null) {
            user.setOptInShare(optIn);
            statusDAO.updateUser(user);
        }
    }
}
