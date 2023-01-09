public class BaseballPlayer {
    private String Name = "";
    private String Team = "";
    private String Position = "";
    private int Height = 0;
    private int Weight = 0;
    private float Age = 0;

    public BaseballPlayer(){

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTeam() {
        return Team;
    }

    public void setTeam(String team) {
        Team = team;
    }

    public String getPosition() {
        return Position;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public int getWeight() {
        return Weight;
    }

    public void setWeight(int weight) {
        Weight = weight;
    }

    public float getAge() {
        return Age;
    }

    public void setAge(float age) {
        Age = age;
    }

    public String toString(){
        return Name + " " + Team + " " + Position + " " + Height + " " + Weight + " " + Age;
    }
}
