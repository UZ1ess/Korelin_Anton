import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        var baseballPlayers = new ArrayList<BaseballPlayer>();
        var allTeams = new HashSet<String>();
        try {
            var reader = new BufferedReader(new FileReader("Показатели спортивных команд.csv"));
            String line;
            reader.readLine();
            while((line = reader.readLine()) != null){
                line = line.replace(" \"\"", "");
                line = line.replace("\"\"", "");
                var playerData = line.split(",");
                var baseballPlayer = new BaseballPlayer();
                baseballPlayer.setName(playerData[0]);
                baseballPlayer.setTeam(playerData[1].replace("\"", ""));
                baseballPlayer.setPosition(playerData[2]);
                baseballPlayer.setHeight(Integer.parseInt(playerData[3]));
                baseballPlayer.setWeight(Integer.parseInt(playerData[4]));
                baseballPlayer.setAge(Float.parseFloat(playerData[5]));
                baseballPlayers.add(baseballPlayer);
                allTeams.add(baseballPlayer.getTeam());
            }
        }
        catch(IOException ignored) {}

        var dbManager = new DBManager();
        dbManager.connectDB();
        dbManager.createDB(allTeams);
        dbManager.writeDB(baseballPlayers);
        dbManager.readDB();
        dbManager.closeDB();
    }
}