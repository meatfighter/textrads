package textrads.ui.question;

import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.input.KeyStroke;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.ArrayUtils;
import textrads.input.InputSource;

import static textrads.ui.common.Images.BIG_BABOON;
import static textrads.ui.common.Images.BIG_BADGER;
import static textrads.ui.common.Images.BIG_CAMEL;
import static textrads.ui.common.Images.BIG_CAT;
import static textrads.ui.common.Images.BIG_CHEETAH;
import static textrads.ui.common.Images.BIG_COW;
import static textrads.ui.common.Images.BIG_DOLPHIN;
import static textrads.ui.common.Images.BIG_DUCK;
import static textrads.ui.common.Images.BIG_FROG;
import static textrads.ui.common.Images.BIG_GERBIL;
import static textrads.ui.common.Images.BIG_GIRAFFE;
import static textrads.ui.common.Images.BIG_GOAT;
import static textrads.ui.common.Images.BIG_GUINEA_PIG;
import static textrads.ui.common.Images.BIG_LEMUR;
import static textrads.ui.common.Images.BIG_LLAMA;
import static textrads.ui.common.Images.BIG_MACAQUE;
import static textrads.ui.common.Images.BIG_OSTRICH;
import static textrads.ui.common.Images.BIG_OWL;
import static textrads.ui.common.Images.BIG_OX;
import static textrads.ui.common.Images.BIG_PANDA;
import static textrads.ui.common.Images.BIG_PIG;
import static textrads.ui.common.Images.BIG_PUPPY;
import static textrads.ui.common.Images.BIG_SEAL;
import static textrads.ui.common.Images.BIG_SNAKE;
import static textrads.ui.common.Images.BIG_TOUCAN;
import static textrads.ui.common.Images.BIG_TURTLE;
import static textrads.ui.common.Images.SMALL_BABOON;
import static textrads.ui.common.Images.SMALL_BADGER;
import static textrads.ui.common.Images.SMALL_CAMEL;
import static textrads.ui.common.Images.SMALL_CAT;
import static textrads.ui.common.Images.SMALL_CHEETAH;
import static textrads.ui.common.Images.SMALL_COW;
import static textrads.ui.common.Images.SMALL_DOLPHIN;
import static textrads.ui.common.Images.SMALL_DUCK;
import static textrads.ui.common.Images.SMALL_FROG;
import static textrads.ui.common.Images.SMALL_GERBIL;
import static textrads.ui.common.Images.SMALL_GIRAFFE;
import static textrads.ui.common.Images.SMALL_GOAT;
import static textrads.ui.common.Images.SMALL_GUINEA_PIG;
import static textrads.ui.common.Images.SMALL_LEMUR;
import static textrads.ui.common.Images.SMALL_LLAMA;
import static textrads.ui.common.Images.SMALL_MACAQUE;
import static textrads.ui.common.Images.SMALL_OSTRICH;
import static textrads.ui.common.Images.SMALL_OWL;
import static textrads.ui.common.Images.SMALL_OX;
import static textrads.ui.common.Images.SMALL_PANDA;
import static textrads.ui.common.Images.SMALL_PIG;
import static textrads.ui.common.Images.SMALL_PUPPY;
import static textrads.ui.common.Images.SMALL_SEAL;
import static textrads.ui.common.Images.SMALL_SNAKE;
import static textrads.ui.common.Images.SMALL_TOUCAN;
import static textrads.ui.common.Images.SMALL_TURTLE;

public class CongratsScreenState {
    
    private final TextImage[][] images = {
        { SMALL_BABOON, BIG_BABOON },
        { SMALL_BADGER, BIG_BADGER },
        { SMALL_CAMEL, BIG_CAMEL },
        { SMALL_CAT, BIG_CAT },
        { SMALL_CHEETAH, BIG_CHEETAH },
        { SMALL_COW, BIG_COW },
        { SMALL_DOLPHIN, BIG_DOLPHIN },
        { SMALL_DUCK, BIG_DUCK },
        { SMALL_FROG, BIG_FROG },
        { SMALL_GERBIL, BIG_GERBIL },
        { SMALL_GIRAFFE, BIG_GIRAFFE },
        { SMALL_GOAT, BIG_GOAT },
        { SMALL_GUINEA_PIG, BIG_GUINEA_PIG },
        { SMALL_LEMUR, BIG_LEMUR },
        { SMALL_LLAMA, BIG_LLAMA },
        { SMALL_MACAQUE, BIG_MACAQUE },
        { SMALL_OSTRICH, BIG_OSTRICH },
        { SMALL_OWL, BIG_OWL },
        { SMALL_OX, BIG_OX },
        { SMALL_PANDA, BIG_PANDA },
        { SMALL_PIG, BIG_PIG },
        { SMALL_PUPPY, BIG_PUPPY },
        { SMALL_SEAL, BIG_SEAL },
        { SMALL_SNAKE, BIG_SNAKE },
        { SMALL_TOUCAN, BIG_TOUCAN },
        { SMALL_TURTLE, BIG_TURTLE },
    };
    
    private final TextField textField = new TextField("Enter your initials:", new InitialsValidator(), 
            new UpperCaseTransformer());
    
    private String title;
    private TextImage smallImage;
    private TextImage bigImage;
    private int imageIndex;
    
    public void init(final String title, final String initials) {
        this.title = title;
        textField.init(initials);
        InputSource.clear();
        if (--imageIndex < 0) {
            imageIndex = images.length - 1;
            ArrayUtils.shuffle(images, ThreadLocalRandom.current());
        }
        smallImage = images[imageIndex][0];
        bigImage = images[imageIndex][1];
    }
    
    public void update() {        
        if (textField.isEnterPressed()) {
            InputSource.clear();
            textField.setCursorVisible(false);
        } else {            
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                textField.handleInput(keyStroke);
            }
            textField.update();
        }
    }

    public String getTitle() {
        return title;
    }

    public TextImage getSmallImage() {
        return smallImage;
    }

    public TextImage getBigImage() {
        return bigImage;
    }

    public TextField getTextField() {
        return textField;
    }
    
    public boolean isEnterPressed() {
        return textField.isEnterPressed();
    }
    
    public String getInitials() {
        return textField.getValue();
    }
}
