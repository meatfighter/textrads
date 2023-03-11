package textrads.ui.menu;

public class MenuItem {
    
    private final String description;
    private final char accelerator;
    
    private boolean selected;
    
    // create spacer
    public MenuItem() {
        this(null, (char) 0);
    }
    
    public MenuItem(final String description) {
        this(description, description.charAt(0));
    }
    
    public MenuItem(final String description, final char accelerator) {
        this.description = description;
        this.accelerator = Character.toUpperCase(accelerator);
    }
    
    public void reset() {
        selected = false;
    }    
    
    public boolean handleInput(final char c) {
        if (accelerator != c) {
            return false;
        }
        selected = true;
        return true;
    }

    public String getDescription() {
        return description;
    }

    public char getAccelerator() {
        return accelerator;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isSpacer() {
        return description == null;
    }
}
