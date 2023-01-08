package textrads;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class EnumUtil {

    private EnumUtil() {        
    }
    
    public static <T extends Enum<T>> T valueOf(final Class<T> enumType, final String name) {
        
        if (isBlank(name)) {
            return null;
        }
        
        try {
            return Enum.valueOf(enumType, name);
        } catch (final IllegalArgumentException ignored) {            
        }
        
        for (final T constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(name)) {
                return constant;
            }
        }
                
        return null;
    }
}
