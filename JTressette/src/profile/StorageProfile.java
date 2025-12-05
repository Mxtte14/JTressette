package profile;



import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author andre
 *
 */
public class StorageProfile
{
    private final String username;
    private String PathImage;
    private Map<String, Object> historyGame; // Map of game data

    public StorageProfile(String username, String password)
    {
        this.username = username;
        this.PathImage = PathImage;
        this.historyGame = new HashMap<>();

    }
    //Getters and Setters
    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return PathImage;
    }


    public void setHistoryGame(String data, String name, String value)
    {
        historyGame.put(data, name + value);
    }


    public Object getHistoryGame(String key)
    {
        return historyGame.get(key);
    }


}

