public class Result {
    private final String name;
    private final int  marks;
    private final double takenTime;

    public Result(String name, int marks, double takenTime){
        this.name = name;
        this.marks = marks;
        this.takenTime = takenTime;
    }

    public String getName() {
        return name;
    }

    public int getMarks() {
        return marks;
    }

    public double getTakenTime() {
        return takenTime;
    }

}
