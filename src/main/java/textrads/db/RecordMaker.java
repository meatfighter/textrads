package textrads.db;

import java.io.Serializable;

public interface RecordMaker<T> extends Serializable {
    
    final long serialVersionUID = 1L;
    
    T make(int index);
}
