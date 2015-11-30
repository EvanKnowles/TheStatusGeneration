package za.co.knonchalant.status;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evan on 15/02/28.
 */
public class SelectedWrapper<T> {
    private T value;

    private boolean selected;

    public SelectedWrapper(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static <G> List<SelectedWrapper<G>> makeWrapper(List<G> rawList) {
        List<SelectedWrapper<G>> wrappedList = new ArrayList<>();

        for(G object : rawList) {
            wrappedList.add(new SelectedWrapper<G>(object));
        }
        return wrappedList;
    }

    public static <G> List<G> getSelected(List<SelectedWrapper<G>> wrappedList) {
        List<G> unwrappedList = new ArrayList<>();

        for(SelectedWrapper<G> object : wrappedList) {
            if (object.isSelected()) {
                unwrappedList.add(object.getValue());
            }
        }

        return unwrappedList;
    }
}
