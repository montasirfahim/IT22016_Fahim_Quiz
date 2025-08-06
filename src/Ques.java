public class Ques {
    String ques, op1, op2, op3, op4;
    int ans; // 1-based index (1–4)

    public Ques(String ques, String op1, String op2, String op3, String op4, int ans) {
        this.ques = ques;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.op4 = op4;
        this.ans = ans;
    }
}
