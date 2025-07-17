import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WordList {
    private List<Word> words; //单词列表
    private Map<String, Integer> wrongWords; //错题统计，单词及错误次数
    private String filePath; //单词文件路径

    public WordList(String filePath) {
        this.filePath = filePath;
        this.words = new ArrayList<>(); //
        this.wrongWords = new HashMap<>();
        loadFromFile(); //自动加载文件
    }

    public void loadFromFile() {
        words.clear(); //单词清理
        wrongWords.clear(); //错题清理
        
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {//修复 确保读码顺利
            String line;
            while ((line = reader.readLine()) != null) { //逐行进行读取直至结束
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String word = parts[0]; //单词
                    String meaning = parts[1]; //对应中文
                    String example = parts[2]; //例句
                    int wrongCount = 0; //错误次数 默认0
                    int reviewCount = 0; //复习次数 默认0
                    
                    if (parts.length >= 4) {
                        try {
                            wrongCount = Integer.parseInt(parts[3]);
                        } catch (NumberFormatException e) { // 忽略错误
                        }
                    }
                    
                    if (parts.length >= 5) {
                        try {
                            reviewCount = Integer.parseInt(parts[4]);
                        } catch (NumberFormatException e) { // 忽略错误
                        }
                    }
                    
                    Word wordObj = new Word(word, meaning, example, wrongCount, reviewCount);
                    words.add(wordObj); //加入到总单词表
                    
                    if (wrongCount > 0) {
                        wrongWords.put(word, wrongCount); //错的加入错题集
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); //显示错误信息
        }
    }

    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Word word : words) {
                writer.write(word.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWord(Word word) {
        words.add(word); //添加新单词进入单词表
        saveToFile(); //并保存至文件夹
    }

    public void removeWord(Word word) {
        words.remove(word);  //在单词表中删除
        wrongWords.remove(word.getWord()); //在错题集中删除
        saveToFile();
    }

    public Word getWord(int index) {
        if (index >= 0 && index < words.size()) {
            return words.get(index);
        }
        return null;
    }
    
    public int size() {
        return words.size(); //单词量
    }


    public void recordWrong(Word word) {
        word.increaseWrongCount(); //错误次数
        wrongWords.put(word.getWord(), word.getWrongCount()); //加入到错题集
        saveToFile(); //保存
    }

    public void recordReview(Word word) {
        word.increaseReviewCount(); //复习次数
        saveToFile(); //保存
    }


    public List<Word> getAllWords() {
        return new ArrayList<>(words); //单词表
    }

    public Map<String, Integer> getWrongWords() {
        return new HashMap<>(wrongWords); //统计错题
    }

    public List<Word> getWrongWordsList() {
        List<Word> wrongList = new ArrayList<>();
        for (Word word : words) {
            if (word.getWrongCount() > 0) {
                wrongList.add(word); //获得错题集
            }
        }
        return wrongList;
    }
} 
