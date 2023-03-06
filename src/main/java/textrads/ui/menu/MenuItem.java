package textrads.ui.menu;

// TODO GET RID OF HIGHLIGHT

public class MenuItem {
    
    private final String description;
    private final char lowerCaseAccelerator;
    private final char upperCaseAccelerator;
    private final int highlightIndex;
    
    // create spacer
    public MenuItem() {
        description = null;
        lowerCaseAccelerator = 0;
        upperCaseAccelerator = 0;
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
        this.lowerCaseAccelerator = Character.toLowerCase(accelerator);
        this.upperCaseAccelerator = Character.toUpperCase(accelerator);
        this.highlightIndex = highlightIndex;
    }

    public String getDescription() {
        return description;
    }

    public char getLowerCaseAccelerator() {
        return lowerCaseAccelerator;
    }

    public char getUpperCaseAccelerator() {
        return upperCaseAccelerator;
    }

    public int getHighlightIndex() {
        return highlightIndex;
    }
    
    public boolean isSpacer() {
        return description == null;
    }
}
