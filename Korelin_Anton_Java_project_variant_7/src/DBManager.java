import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;


public class DBManager {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    private HashSet<String> allTeams = new HashSet<>();
    private ArrayList<String[]> teamAverAge = new ArrayList<>();
    private final ArrayList<String[]> teamAverHeight = new ArrayList<>();
    private final ArrayList<String[]> teamAverWeight = new ArrayList<>();

    // --------Подключение к базе данных--------
    public void connectDB() throws ClassNotFoundException, SQLException
    {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:BaseballPlayers.s3db");
    }

    // --------Создание таблиц--------
    public void createDB(HashSet<String> teams) throws SQLException
    {
        allTeams = teams;
        statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS persons;");
        statement.execute("CREATE TABLE if not exists 'persons' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text," +
                " 'height' INT, 'weight' INT, 'age' REAL);");

        for (var team:allTeams){
            statement.execute("DROP TABLE IF EXISTS " + team + ";");
            statement.execute("CREATE TABLE if not exists '" + team + "' ('position' text, 'name' text);");
        }
    }

    // --------Заполнение таблиц--------
    public void writeDB(ArrayList<BaseballPlayer> players) throws SQLException
    {
        for (var player: players){
            var name = player.getName().replace("'", "''");
            var team = player.getTeam();
            var position = player.getPosition();
            var height = player.getHeight();
            var weight = player.getWeight();
            var age = player.getAge();
            var ex = "INSERT INTO 'persons' ('name', 'height', 'weight', 'age') VALUES (" +
                    "'" + name + "', " + height + ", " +
                    "" + weight + ", " + age + "); ";
            statement.execute(ex);
            ex = "INSERT INTO '" + team + "' ('position', 'name') VALUES (" +
                    "'" + position + "', '" + name + "'); ";
            statement.execute(ex);
        }
    }

    // -------- Чтение таблиц, поиск и вывод необходимых данных--------
    public void readDB() throws SQLException, IOException
    {
        //Поиск среднего возраста команд
        for (var team: allTeams) {
            resultSet = statement.executeQuery("SELECT * FROM " + team);
            var sumOfAges = 0.0;
            var counter = 0;
            var names = new ArrayList<String>();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                names.add(name.replace("'", "''"));
            }
            for (var name : names) {
                resultSet = statement.executeQuery("SELECT * FROM persons WHERE name = '" + name + "'");
                String age = null;
                if (resultSet.next())
                    age = resultSet.getString("age");
                if (age != null) {
                    sumOfAges += Double.parseDouble(age);
                    counter++;
                }
            }
            teamAverAge.add(new String[]{team, String.valueOf(sumOfAges / counter)});
        }


        //Создание и сохранение графика по среднему возрасту комманд
        sort(teamAverAge);
        teamAverAge = reverse(teamAverAge);
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        for(var teamData:teamAverAge){
            dataset.addValue( Double.parseDouble(teamData[1]) , teamData[0] , "" );
        }
        JFreeChart barChart = ChartFactory.createBarChart("График средних возрастов игроков команд", "Название команды",
                "Средний возраст игроков команды", dataset,PlotOrientation.VERTICAL, true, false, false);
        File BarChart = new File( "BaseballTeamsAverAgesChart.png" );
        ChartUtils.saveChartAsPNG( BarChart , barChart , 800 , 600);
        System.out.println();
        System.out.println("I");
        System.out.println("Построен график по среднему возрасту во всех командах");
        System.out.println("Сохранен в папку проекта в виде .png файла под названием \"BaseballTeamsAverAgesChart\"");


        //Поиск среднего роста команд
        for (var team: allTeams){
            resultSet = statement.executeQuery("SELECT * FROM " + team);
            var sumOfHeights = 0.0;
            var counter = 0;
            var names = new ArrayList<String>();
            while(resultSet.next())
            {
                String name = resultSet.getString("name");
                names.add(name.replace("'", "''"));
            }
            for(var name: names){
                resultSet = statement.executeQuery("SELECT * FROM persons WHERE name = '" + name + "'");
                String height = null;
                if (resultSet.next())
                    height = resultSet.getString("height");
                if (height != null){
                    sumOfHeights += Double.parseDouble(height);
                    counter++;
                }
            }
            teamAverHeight.add(new String[] {team, String.valueOf(sumOfHeights / counter)});
        }


        //Поиск команды с максимальным средним ростом
        var max = Double.MIN_VALUE;
        for (var e: teamAverHeight){
            if (Double.parseDouble(e[1]) > max){
                max = Double.parseDouble(e[1]);
            }
        }
        var teamName = "";
        for (var e: teamAverHeight){
            if (Double.parseDouble(e[1]) == max){
                teamName = e[0];
                break;
            }
        }


        //Поиск пяти самых высоких игроков команды с самым высоким средним ростом
        resultSet = statement.executeQuery("SELECT * FROM " + teamName);
        var names = new ArrayList<String>();
        while(resultSet.next())
        {
            String name = resultSet.getString("name");
            names.add(name.replace("'", "''"));
        }
        var nameHeight = new ArrayList<String[]>();
        for(var name:names){
            resultSet = statement.executeQuery("SELECT * FROM persons WHERE name = '" + name + "'");
            var height = resultSet.getString("height");
            nameHeight.add(new String[] {name, height});
        }


        //Сортировка и вывод 5 самых высоких игроков команды с самым высоким средним ростом
        sort(nameHeight);
        System.out.println();
        System.out.println("II");
        System.out.println("Команда с самым высоким средним ростом игроков: " + teamName);
        System.out.println("Пять самых высоких игроков этой команды: ");
        for(var i = 0; i < 5; i++){
            System.out.println((i + 1) + ". Имя: " + nameHeight.get(i)[0] + ", Рост(в дюймах): " + nameHeight.get(i)[1]);
        }
        System.out.println();


        //Поиск среднего веса игроков команд
        for (var team: allTeams){
            resultSet = statement.executeQuery("SELECT * FROM " + team);
            var sumOfWeights = 0.0;
            var counter = 0;
            names = new ArrayList<>();
            while(resultSet.next())
            {
                String name = resultSet.getString("name");
                names.add(name.replace("'", "''"));
            }

            for(var name: names){
                resultSet = statement.executeQuery("SELECT * FROM persons WHERE name = '" + name + "'");
                String weight = null;
                if (resultSet.next())
                    weight = resultSet.getString("weight");
                if (weight != null){
                    sumOfWeights += Double.parseDouble(weight);
                    counter++;
                }
            }
            teamAverWeight.add(new String[] {team, String.valueOf(sumOfWeights / counter)});
        }


        //Поиск команд со средним ростом 74 - 78
        var teamsWithSearchingHeight = new ArrayList<String[]>();
        for(var team: teamAverHeight){
            if (Double.parseDouble(team[1]) >= 74 && Double.parseDouble(team[1]) <= 78){
                teamsWithSearchingHeight.add(team);
            }
        }


        //Поиск команд со средним весом 190 - 210
        var teamsWithSearchingWeight = new ArrayList<String[]>();
        for(var team:teamAverWeight){
            if (Double.parseDouble(team[1]) >= 190 && Double.parseDouble(team[1]) <= 210){
                teamsWithSearchingWeight.add(team);
            }
        }


        //Поиск и вывод команды с самым высоким средним возрастом из предыдущих
        sort(teamAverAge);
        for (String[] strings : teamAverAge) {
            var averHeight = 0.0;
            var averWeight = 0.0;
            for (var team : teamsWithSearchingWeight) {
                if (team[0].equals(strings[0])) {
                    averWeight = Double.parseDouble(team[1]);
                }
            }
            for (var team : teamsWithSearchingHeight) {
                if (team[0].equals(strings[0])) {
                    averHeight = Double.parseDouble(team[1]);
                }
            }
            if (averWeight != 0 && averHeight != 0) {
                System.out.println("III");
                System.out.println("Команда с средним ростом равным от 74 до 78 inches и средним весом от 190 до 210 lbs " +
                        "с самым высоким средним возрастом:");
                System.out.println("Название команды: " + strings[0] + ", Средний вес игроков этой команды: "
                        + averWeight + ", Средний рост игроков этой команды: " + averHeight);
                break;
            }
        }
        System.out.println();
    }

    // --------Отключение от базы данных--------
    public void closeDB() throws SQLException
    {
        if (connection != null)
            connection.close();
        if (statement != null)
            statement.close();
        if (resultSet != null)
            resultSet.close();
    }

    private void sort(ArrayList<String[]> list){
        var marker = false;
        while(!marker){
            marker = true;
            var index1 = 0;
            var index2 = 1;
            while(index2 < list.size()){
                var value1 = list.get(index1);
                var value2 = list.get(index2);
                if (Double.parseDouble(value2[1]) > Double.parseDouble(value1[1])){
                    list.set(index1, value2);
                    list.set(index2, value1);
                    marker = false;
                }
                index1++;
                index2++;
            }
        }
    }

    private ArrayList<String[]> reverse(ArrayList<String[]> list){
        var reversedList = new ArrayList<String[]>();
        for(var i = list.size() - 1; i > -1; i--){
            reversedList.add(list.get(i));
        }
        return reversedList;
    }
}