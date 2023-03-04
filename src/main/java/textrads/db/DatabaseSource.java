package textrads.db;

public final class DatabaseSource {

    private static final Database database = new Database();
    
    static {
        database.init();
    }
    
    public static Database getDatabase() {
        return database;
    }
    
    private DatabaseSource() {        
    }
}
