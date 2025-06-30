public class Quiz {
    public int qid, correctOption;
    public String ques, op1, op2, op3;

    public Quiz(int qid, String ques, String op1, String op2, String op3, int correctOption){
        this.qid = qid;
        this.ques = ques;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.correctOption = correctOption;
    }

}
