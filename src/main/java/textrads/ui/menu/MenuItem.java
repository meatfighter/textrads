package textrads.ui.menu;

public class MenuItem {
    
    private final String description;
    private final char accelerator;
    private final int highlightIndex;
    
    // create spacer
    public MenuItem() {
        description = null;
        accelerator = 0;
        highlightIndex = 0;
    }
    
    public MenuItem(final String description) {
        this(description, description.charAt(0));
    }
    
    public MenuItem(final String description, final char accelerator) {
        this(description, accelerator, description.toLowerCase().indexOf(Character.toLowerCase(accelerator)));
    }
    
    public MenuItem(final String description, final char accelerator, final int highlightIndex) {
        this.description = description;
        this.accelerator = Character.toLowerCase(accelerator);
        this.highlightIndex = highlightIndex;
    }

    public String getDescription() {
        return description;
    }

    public char getAccelerator() {
        return accelerator;
    }

    public int getHighlightIndex() {
        return highlightIndex;
    }
    
    public boolean isSpacer() {
        return description == null;
    }
}
