package src;
public class Word {
    private String word; // 单词
    private String meaning; // 单词含义
    private String example; // 例句
    private int wrongCount; // 错误次数
    private int reviewCount; // 复习次数

    public Word(String word, String meaning, String example) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.wrongCount = 0; //初始化错误次数0
        this.reviewCount = 0; //初始化复习次数0
    }

 
    public Word(String word, String meaning, String example, int wrongCount, int reviewCount) {
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.wrongCount = wrongCount; //错误次数
        this.reviewCount = reviewCount; //复习错误
    }

    public String getWord() {
        return word; //对比词
    }

    public void setWord(String word) {
        this.word = word; //正确词
    }

    public String getMeaning() {
        return meaning; 
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning; 
    }

    public String getExample() {
        return example; 
    }

    public void setExample(String example) {
        this.example = example; 
    }

    public int getWrongCount() {
        return wrongCount; 
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount; 
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void increaseWrongCount() {
        this.wrongCount++; //错误次数加
    }

    public void increaseReviewCount() {
        this.reviewCount++; //复习次数加
    }

    @Override
    public String toString() {
        return word + "," + meaning + "," + example + "," + wrongCount + "," + reviewCount;
    }
} 