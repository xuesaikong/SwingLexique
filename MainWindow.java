import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Properties;

public class MainWindow extends JFrame { // 常量定义
    private static final String HOME_PANEL = "主页"; //主页
    private static final String DICT_PANEL = "词表管理"; //词表管理
    private static final String STUDY_PANEL = "单词学习"; //单词学习
    private static final String WRONG_PANEL = "错题本"; //错题本
    private static final String CONFIG_FILE = "config.properties"; //保存当前选择的文件路径

    private WordList currentWordList; //核心数据
    private String currentDictPath = "words.csv"; // 默认词典路径

    private JPanel cardPanel; //卡片布局 各个功能面板
    private CardLayout cardLayout; //不同功能界面切换
 
    private JPanel homePanel; //主页界面
    private JPanel dictPanel; //词库管理面板
    private JTable wordTable; //显示单词列表表格
    private DefaultTableModel tableModel; //表格数据
    private JComboBox<String> dictComboBox; //选择框下拉
    private JTextField wordField; //单词文本框
    private JTextField meaningField; //释义文本框
    private JTextField exampleField; //例句文本框
   
    private JPanel studyPanel; //学习面板
    private JLabel totalWordsLabel; //已学习的单词数量
    private JLabel correctCountLabel; //回答正确的数量
    private JLabel wrongCountLabel; //回答错误的数量
    private JLabel currentWordLabel; //当前学习单词
    private JLabel meaningLabel; //中文释义
    private JLabel exampleLabel; //例句
    private JTextField answerField; //输入答案的输入框
    private JButton nextButton; //下一个按钮
    private int totalStudied = 0; //学习总数量
    private int correctCount = 0; //正确数量总数
    private int wrongCount = 0; //错误总数
    private Word currentWord; //正在学习单词
    
    private JPanel wrongPanel; //错题版主页面
    private JTable wrongTable; //错题表格
    private DefaultTableModel wrongTableModel; //错误表格数据
    private JComboBox<String> wrongDictComboBox; // 错题本下拉选择框

    public MainWindow() { // 设置窗口标题和大小
        super("单词学习系统"); //标题
        setSize(800, 600); //窗口尺寸
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //退出
        setLocationRelativeTo(null); //窗口居中显示

        Font globalFont = new Font("Dialong", Font.PLAIN, 12); //设置UI字体，支持法语
        UIManger.put("Button.Font", globalFont);
        UIManger.put("Label.Font", globalFont);
        UIManger.put("TextField.Font", globalFont);
        UIManger.put("TextArea.Font", globalFont);
        UIManger.put("Table.Font", globalFont);
        UIManger.put("ComboBox.Font", globalFont);

        loadConfig(); //加载配置
        
        currentWordList = new WordList(currentDictPath); // 初始化单词列表

        initComponents();     // 初始化界面组件
    }

    private void initComponents() { // 创建菜单栏
        createMenuBar();

        cardLayout = new CardLayout(); //布局切换
        cardPanel = new JPanel(cardLayout); //主容器 卡片布局

        createHomePanel(); //主页页面
        createDictPanel(); //词库页面
        createStudyPanel(); //学习页面
        createWrongPanel(); //错题本页面

        cardPanel.add(homePanel, HOME_PANEL);
        cardPanel.add(dictPanel, DICT_PANEL);
        cardPanel.add(studyPanel, STUDY_PANEL);
        cardPanel.add(wrongPanel, WRONG_PANEL); //添加页面到卡片布局 

        setContentPane(cardPanel); //设置内容面板

        cardLayout.show(cardPanel, HOME_PANEL); //默认显示主页
    }
 
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar(); //创建菜单栏

        JMenu fileMenu = new JMenu("文件"); //文件菜单
        JMenuItem exitItem = new JMenuItem("退出"); //退出
        exitItem.addActionListener(e -> System.exit(0)); //退出后关闭程序
        fileMenu.add(exitItem); //添加退出到菜单

        JMenu pageMenu = new JMenu("页面"); //创建页面菜单
        
        JMenuItem homeItem = new JMenuItem(HOME_PANEL); //主页菜单
        homeItem.addActionListener(e -> cardLayout.show(cardPanel, HOME_PANEL)); //切换主页面
        
        JMenuItem dictItem = new JMenuItem(DICT_PANEL); 
        dictItem.addActionListener(e -> { 
            loadDictionaries(); //加载所有词典文件
            loadWordData(); //加载词典中所有单词
            cardLayout.show(cardPanel, DICT_PANEL); //切换到词典管理
        });
        
        JMenuItem studyItem = new JMenuItem(STUDY_PANEL);
        studyItem.addActionListener(e -> {
            if (currentWordList.size() > 0) { //检验是否有单词
                resetStudyStats(); //学习数据重制
                loadNextWord(); //单词继续加载
                cardLayout.show(cardPanel, STUDY_PANEL); //切换学习页面
            } else {
                JOptionPane.showMessageDialog(this, "当前词典没有单词，请先添加单词！", "提示", JOptionPane.WARNING_MESSAGE); //没有单词时提示
            }
        });
        
        JMenuItem wrongItem = new JMenuItem(WRONG_PANEL);
        wrongItem.addActionListener(e -> {
            loadWrongWords(); //错题本加载
            cardLayout.show(cardPanel, WRONG_PANEL); //切换到错题本页面
        });
        
        pageMenu.add(homeItem); //主页添加到页面菜单
        pageMenu.add(dictItem); //词典添加到页面菜单
        pageMenu.add(studyItem); //学习添加到页面菜单
        pageMenu.add(wrongItem); //错题本添加到页面菜单
        
        menuBar.add(fileMenu);
        menuBar.add(pageMenu); // // 添加菜单到菜单栏
        
        setJMenuBar(menuBar);  // 设置菜单栏
    }
 
    private void createHomePanel() {
        homePanel = new JPanel(new BorderLayout()); //主页面板 边界布局
        
        JLabel titleLabel = new JLabel("单词学习系统", JLabel.CENTER); //顶部标题
        titleLabel.setFont(new Font("宋体", Font.BOLD, 36)); //字体和大小
        homePanel.add(titleLabel, BorderLayout.NORTH); //位于顶部
        
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 30));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150)); // 创建功能按钮面板，距离内边距
        
        JButton dictButton = new JButton("词表管理"); //词表顶部标题
        dictButton.setFont(new Font("宋体", Font.PLAIN, 24)); //字体和大小
        dictButton.addActionListener(e -> {
            loadDictionaries(); //加载词库
            loadWordData(); //加载单词
            cardLayout.show(cardPanel, DICT_PANEL); //切换词库页面
        });
        
        JButton studyButton = new JButton("单词学习"); //错题本按钮
        studyButton.setFont(new Font("宋体", Font.PLAIN, 24)); //字体和大小
        studyButton.addActionListener(e -> {
            if (currentWordList.size() > 0) {
                resetStudyStats(); //重置学习情况
                loadNextWord(); //加载下一代单词
                cardLayout.show(cardPanel, STUDY_PANEL); //切换到学习页面
            } else {
                JOptionPane.showMessageDialog(this, "当前词典没有单词，请先添加单词！", "提示", JOptionPane.WARNING_MESSAGE); //没有单词，提示错误
            }
        });
        
        JButton wrongButton = new JButton("错题本"); //创建错题本
        wrongButton.setFont(new Font("宋体", Font.PLAIN, 24)); //字体和大小
        wrongButton.addActionListener(e -> { 
            loadWrongWords(); //加载错题本
            cardLayout.show(cardPanel, WRONG_PANEL); //切换错题本页面
        });
        
        buttonPanel.add(dictButton); //添加词表管理按钮
        buttonPanel.add(studyButton); //添加单词学习按钮
        buttonPanel.add(wrongButton); //添加错题本按钮
        
        homePanel.add(buttonPanel, BorderLayout.CENTER); //以上按钮添加至主页
    }
 
    private void createDictPanel() { //创建此表管理界面
        dictPanel = new JPanel(new BorderLayout(10, 10)); //字体和大小
        dictPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //内边距

        JPanel topPanel = new JPanel(new BorderLayout()); //顶部面板

        JPanel dictSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 词典选择面板
        dictSelectPanel.add(new JLabel("选择词典:")); //添加选择词典标签
        dictComboBox = new JComboBox<>(); //创建下拉框选择词典
        dictComboBox.setPreferredSize(new Dimension(200, 25)); //宽高
        dictComboBox.addActionListener(e -> {
            if (dictComboBox.getSelectedItem() != null) {
                currentDictPath = dictComboBox.getSelectedItem().toString(); //词典路径
                currentWordList = new WordList(currentDictPath); //加载对应词典
                loadWordData(); //加载词典内容到表格
            }
        });
        dictSelectPanel.add(dictComboBox); //左侧区域添加下拉框
        
        JButton newDictButton = new JButton("新建词典"); //新建词典按钮
        newDictButton.addActionListener(e -> createNewDictionary()); //调用新建词典
        dictSelectPanel.add(newDictButton);

        JButton deleteDictButton = new JButton("删除词典"); //新建删除字典按钮
        deleteDictbutton.addActionListener(e -> createNewDictionary()); //调用删除字典
        dictSelectPanel.add(deleteDictButton); //按钮谈驾到词典选择的面板上
        
        topPanel.add(dictSelectPanel, BorderLayout.WEST); //左侧词典选择区域加入顶部面板

        JButton homeButton = new JButton("返回主页"); //返回主页按钮
        homeButton.addActionListener(e -> cardLayout.show(cardPanel, HOME_PANEL)); //切换主页
        topPanel.add(homeButton, BorderLayout.EAST); //添加到主页面板右侧
        
        dictPanel.add(topPanel, BorderLayout.NORTH); //顶部加入到词典主页面
        
        String[] columnNames = {"单词", "含义", "例句", "错误次数", "复习次数"}; //创建表格
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 表格不可编辑
            }
        };

        wordTable = new JTable(tableModel); //创建表格
        wordTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //允许多选
        JScrollPane scrollPane = new JScrollPane(wordTable); //创建滚动条
        dictPanel.add(scrollPane, BorderLayout.CENTER); //添加表格到中部区域

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("添加/编辑单词")); //创建输入

        inputPanel.add(new JLabel("单词:")); 
        wordField = new JTextField();
        inputPanel.add(wordField); //单词输入

        inputPanel.add(new JLabel("含义:"));
        meaningField = new JTextField();
        inputPanel.add(meaningField); //中文释义输入

        inputPanel.add(new JLabel("例句:"));
        exampleField = new JTextField();
        inputPanel.add(exampleField); //例句输入
        
        JPanel buttonPanel = new JPanel(new FlowLayout()); //创建按钮面板
        JButton addButton = new JButton("添加单词"); //添加单词按钮
        addButton.addActionListener(e -> addWord()); //添加单词
        buttonPanel.add(addButton); //添加单词按钮添加到底部

        JButton editButton = new JButton("编辑单词"); //添加编辑单词按钮
        editButton.addActionListener(e -> editWord()); //编辑单词
        buttonPanel.add(editButton); //编辑单词按钮到底部
        
        JButton deleteButton = new JButton("删除单词"); //添加删除单词按钮
        deleteButton.addActionListener(e -> deleteWord()); //删除单词
        buttonPanel.add(deleteButton); //删除单词按钮到底部地步

        JButton batchDeleteButton = new JButton("批量删除"); //添加批量删除按钮
        batchDeleteButton.addActionListener(e -> batchDeleteWords()); //批量删除
        buttonPanel.add(batchDeleteButton); // 按钮到底部按钮面板中
        
        JButton importButton = new JButton("批量导入"); //批量导入按钮
        importButton.addActionListener(e -> importFromCSV()); //用csv方法进行导入
        buttonPanel.add(importButton); //批量导入按钮到底部
        
        JPanel bottomPanel = new JPanel(new BorderLayout()); //底部面板 边界布局 放置输入和按钮
        bottomPanel.add(inputPanel, BorderLayout.CENTER); //单词中文释义例句放在面板底部居中
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);  //添加编辑删除放在底部区域

        dictPanel.add(bottomPanel, BorderLayout.SOUTH); //底部面板添加到词典管理底部
    }

    private void createStudyPanel() { //创建单词学习主页版
        studyPanel = new JPanel(new BorderLayout(10, 10)); //边界布局
        studyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //边框留白

        JPanel topPanel = new JPanel(new BorderLayout()); // 进行面板创建
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //统计信息区域创建总单词 已学习 答错数据 居左
        statsPanel.add(new JLabel("总单词数:")); //总单词书静态
        totalWordsLabel = new JLabel("0"); //更新总数
        statsPanel.add(totalWordsLabel);
        
        statsPanel.add(new JLabel("    已学习:"));
        JLabel studiedLabel = new JLabel("0"); //更更新已学习单词数量
        statsPanel.add(studiedLabel);
        
        statsPanel.add(new JLabel("    答对:"));
        correctCountLabel = new JLabel("0"); //更新答对数量
        statsPanel.add(correctCountLabel);
        
        statsPanel.add(new JLabel("    答错:"));
        wrongCountLabel = new JLabel("0"); //更新答错数量
        statsPanel.add(wrongCountLabel);
        
        topPanel.add(statsPanel, BorderLayout.WEST); //以上统计信息添加到左侧
        
        JButton homeButton = new JButton("返回主页"); //返回主页按钮
        homeButton.addActionListener(e -> {
            showStudyResult(); //当前学习结果
            cardLayout.show(cardPanel, HOME_PANEL); //转到主页
        });
        topPanel.add(homeButton, BorderLayout.EAST); //主页按钮位于顶部面板右侧
        
        studyPanel.add(topPanel, BorderLayout.NORTH); //顶部的面板添加到学习页面的顶部
        
        JPanel centerPanel = new JPanel(); //中间面板
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS)); //垂直排列
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // 四周边距的像素设定为50
        
        currentWordLabel = new JLabel("", JLabel.CENTER); //创建空标签，居中
        currentWordLabel.setFont(new Font("宋体", Font.BOLD, 36)); //字体大小
        currentWordLabel.setAlignmentX(Component.CENTER_ALIGNMENT); //水平方向居中对齐
        centerPanel.add(currentWordLabel); //添加到中间面板
        
        centerPanel.add(Box.createVerticalStrut(20)); //垂直间隔20像素

        meaningLabel = new JLabel("", JLabel.CENTER); //单词含义初始为空
        meaningLabel.setFont(new Font("宋体", Font.PLAIN, 24)); //字体及大小
        meaningLabel.setAlignmentX(Component.CENTER_ALIGNMENT); //居中对齐
        meaningLabel.setVisible(false); //初始不可见
        centerPanel.add(meaningLabel); //添加到面板中
        
        centerPanel.add(Box.createVerticalStrut(10)); //10像素间隔

        exampleLabel = new JLabel("", JLabel.CENTER); //显示例句标签
        exampleLabel.setFont(new Font("宋体", Font.ITALIC, 18)); //字体斜体及大小
        exampleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); //居中
        exampleLabel.setVisible(false); //初始不可见
        centerPanel.add(exampleLabel); //添加到面板中
        
        centerPanel.add(Box.createVerticalStrut(30)); //间隔

        JPanel answerPanel = new JPanel(); //创建大答案输入子面板
        answerPanel.setLayout(new BoxLayout(answerPanel, BoxLayout.X_AXIS)); //水平排列
        answerPanel.setAlignmentX(Component.CENTER_ALIGNMENT); //居中对齐
        
        answerPanel.add(new JLabel("输入含义: ")); //添加提示标签
        answerField = new JTextField(20); //创建输入框，长度20
        answerField.setMaximumSize(new Dimension(300, 30)); //尺寸
        answerPanel.add(answerField); //添加输入框到面板中
        
        JButton checkButton = new JButton("检查"); //创建检查按钮
        checkButton.addActionListener(e -> checkAnswer()); //调用checkAnwer判断错误
        answerPanel.add(checkButton); //按钮在面板中
        
        centerPanel.add(answerPanel); //答题区域添加到中间面板
        
        studyPanel.add(centerPanel, BorderLayout.CENTER); //中间面板添加到学习页面中部

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 创建底部面板
        
        nextButton = new JButton("下一个"); //创建下一个按钮
        nextButton.setEnabled(false); //初始不可点击
        nextButton.addActionListener(e -> {
            loadNextWord(); //加载下一个单词
            nextButton.setEnabled(false); //再次不可点击，等待新回答
        });
        bottomPanel.add(nextButton); //添加按钮到底部面板
        
        studyPanel.add(bottomPanel, BorderLayout.SOUTH); //将底部面板添加到学习页面底部
    }
   
    private void createWrongPanel() { //创建错题本页面
        wrongPanel = new JPanel(new BorderLayout(10, 10)); //主面板，边距
        wrongPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //内边距

        JPanel topPanel = new JPanel(new BorderLayout()); //顶部标题面板
        JPanel dictSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //左对齐面板 防止词典下拉框
        dictSelectPanel.add(new JLabel("选择词典:")); //添加选择词典
        wrongDictComboBox = new JComboBox<>(); //创建下拉框
        wrongDictComboBox.setPreferredSize(new Dimension(200, 25)); //cchicun
        wrongDictComboBox.addActionListener(e -> { //执行操作
            if (wrongDictComboBox.getSelectedItem() != null) {
                currentDictPath = wrongDictComboBox.getSelectedItem().toString(); //获取字典并更新
                currentWordList = new WordList(currentDictPath); //根据所选创建新文件
                saveConfig(); //保存
                loadWrongWords(); //加载
            }
        });
        dictSelectPanel.add(wrongDictComboBox); //下拉框添加至面板
        topPanel.add(titleLabel, BorderLayout.CENTER); //面板放置在错题本页面左上
        
        JLabel titleLabel = new JLabel("错题本", JLabel.CENTER); //创建标题标签
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24)); //字体及大小
        topPanel.add(titleLabel, BorderLayout.CENTER); //添加到中间位置
        
        JButton homeButton = new JButton("返回主页"); //创建返回主页按钮
        homeButton.addActionListener(e -> cardLayout.show(cardPanel, HOME_PANEL)); //返回首页
        topPanel.add(homeButton, BorderLayout.EAST); //添加到右侧
        
        wrongPanel.add(topPanel, BorderLayout.NORTH); //添加顶部面板于错题本页面

        String[] columnNames = {"单词", "含义", "例句", "错误次数", "复习次数"}; //// 创建单词含义例句错误次数表格的表头
        wrongTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  //表格不可编辑
            }
        };
        wrongTable = new JTable(wrongTableModel); //创建表格
        JScrollPane scrollPane = new JScrollPane(wrongTable); //添加滚条
        wrongPanel.add(scrollPane, BorderLayout.CENTER); //添加表格到页面中样

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 创建按钮面板
        
        JButton exportButton = new JButton("导出错题"); //创建导出错题按钮
        exportButton.addActionListener(e -> exportWrongWords()); //点击后使用
        buttonPanel.add(exportButton); //添加到按钮版面中
        
        JButton removeButton = new JButton("移出错题本"); //创建移出错题本按钮
        removeButton.addActionListener(e -> removeFromWrongList()); //点击后使用
        buttonPanel.add(removeButton); //添加到面板
        
        wrongPanel.add(buttonPanel, BorderLayout.SOUTH); //按钮页面添加到底部
    }
    
    private void loadDictionaries() { //加载当前的目录中含有的词典
        dictComboBox.removeAllItems(); //清空下拉框原有项目
        wrongDictComboBox.removeAllItems(); //清空

        File dir = new File("."); // 获取当前目录下
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv")); //筛选.csv文件
        
        if (files != null) {
            for (File file : files) {
                dictComboBox.addItem(file.getName()); //每个文件名添加到下拉列表框
                wrongDictComboBox.addItem(file.getName()); //错词典名添加到错题本下拉框
            }
        }

        dictComboBox.setSelectedItem(currentDictPath); /// 设置当前词典
        wrongDictComboBox.setSelectedItem(currentDictPath); //设置错误词典
    }

    private void loadConfig() {
        Properties props = new Properties(); //创建存储
        File configFile = new File(CONFIG_FILE); //创建File

        if (configFile.exists()) { //文件存在
            try (FileInputStream fis = new FileInputStream(configFile)) { //打开文件进行输入
                props.load(fis); //文件内容放入加载
                currentDictPath = props.getProperty("currentDictPath", "words.csv"); //进行读取 无则使用默认文件
            } catch (IOException e) { //过程出错
                e.printStackTrace(); //加载失败默认值
                currentDictPath = "words.csv"; //出错 使用默认文件
            }
        }
    }

    private void createNewDictionary() { 
        Properties props = new Properties(); //创建对象 用于保存
        props.setProperty("currentDictPath", currentDictPath); 

         try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) { //设置匹配项
             props.store(fos, "Word Learning System Configuration"); //保存到文件

         } catch (IOException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "保存配置失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE); //错误后提示
         }
    }
 
    private void createNewDictionary() { //创建新词典文件
        String name = JOptionPane.showInputDialog(this, "请输入新词典名称（不含.csv）:", "新建词典", JOptionPane.QUESTION_MESSAGE); //提示输入词典名
        if (name != null && !name.trim().isEmpty()) { ////判断文件名是否有效
            String dictName = name.trim() + ".csv"; //不含.csv
            File file = new File(dictName); //创建对象
            
            if (file.exists()) {
                JOptionPane.showMessageDialog(this, "词典已存在！", "错误", JOptionPane.ERROR_MESSAGE); //已存在，提示错误
                return;
            }
            
            currentDictPath = dictName; //当前词典路径
            currentWordList = new WordList(currentDictPath); //创建新单词表
            currentWordList.saveToFile(); // 创建（空）文件

            saveConfig(); //保存当前词典配置
            loadDictionaries(); //刷新下拉框内容
            dictComboBox.setSelectedItem(currentDictPath); //选中新建词典
            wrongDictComboBox.setSelectedItem(currentDictPath); //下拉框的选中项设置为当前的词典存储路径
            loadWordData(); //加载新数据
        }
    }
    
    private void loadWordData() { //加载单词数据到表格
        tableModel.setRowCount(0);  // 清空表格
        
        List<Word> words = currentWordList.getAllWords(); //获取当前词典中单词
        for (Word word : words) {
            Object[] rowData = {
                word.getWord(), //单词
                word.getMeaning(), //中文释义
                word.getExample(), //例句
                word.getWrongCount(), //错误次数
                word.getReviewCount() //复习次数
            };
            tableModel.addRow(rowData); //按照以上要求添加到表格
        }
    }

    private void addWord() { //添加单词
        String word = wordField.getText().trim(); //用户输入单词后获取后去掉空格
        String meaning = meaningField.getText().trim(); ///用户输入中文释义后获取后去掉空格
        String example = exampleField.getText().trim(); //用户输入例句后获取后去掉空格
        
        if (word.isEmpty() || meaning.isEmpty()) { //单词为空
            JOptionPane.showMessageDialog(this, "单词和含义不能为空！", "错误", JOptionPane.ERROR_MESSAGE); //提示错误
            return;
        }
        
        Word newWord = new Word(word, meaning, example); //创建新的单词
        currentWordList.addWord(newWord); //添加到字典
        
        wordField.setText("");
        meaningField.setText("");
        exampleField.setText(""); //清空输入框
        
        loadWordData(); // 刷新表格
    }

    private void editWord() { //编辑单词
        int selectedRow = wordTable.getSelectedRow(); //获取选择单词
        if (selectedRow == -1) { //用户无选择
            JOptionPane.showMessageDialog(this, "请选择要编辑的单词！", "提示", JOptionPane.INFORMATION_MESSAGE); //提示
            return;
        }

        Word word = currentWordList.getWord(selectedRow); // 获取选中的单词
        if (word == null) return;
        
        wordField.setText(word.getWord()); // 单词输入
        meaningField.setText(word.getMeaning()); //中文释义输入
        exampleField.setText(word.getExample()); //例句输入

        currentWordList.removeWord(word); //// 删除旧单词
 
        loadWordData(); //刷新表格
    }

    private void deleteWord() { //删除单词
        int selectedRow = wordTable.getSelectedRow(); //获取选中单词
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的单词！", "提示", JOptionPane.INFORMATION_MESSAGE); //提示确认
            return;
        }
        
        Word word = currentWordList.getWord(selectedRow); //确认删除
        if (word != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要删除单词 '" + word.getWord() + "' 吗？", "确认删除", JOptionPane.YES_NO_OPTION); //确认
            if (confirm == JOptionPane.YES_OPTION) {
                currentWordList.removeWord(word); //执行删除
                loadWordData(); //刷新列表
            }
        }
    }

    private void importFromCSV() { //从CSV文件批量导入单词
        if (dictComboBox.getSelectedItem() == null) { // 检查是否已选择词典
            JOptionPane.showMessageDialog(this, "请先选择一个词典！", "提示", JOptionPane.WARNING_MESSAGE); //提示
            return;
        }

        JFileChooser fileChooser = new JFileChooser(); // 打开文件选择器
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV文件", "csv")); 
        int result = fileChooser.showOpenDialog(this); //选择结果
        
        if (result == JFileChooser.APPROVE_OPTION) { //选择打开
            File selectedFile = fileChooser.getSelectedFile(); //执行选择文件
            try {
                int importCount = 0; //导入数
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(selectedFile), StandardCharsets.UTF_8))) { //使用UTF_8读取
                    String line;// 跳过第一行（表头）
                    reader.readLine(); //逗号分隔
                    while ((line = reader.readLine()) != null) { //逐个读取
                        if（line = reader.readline()) !=null{ //空行跳过
                            continue; //继续下一行
                        }

                        string[]parts = line.split(",") //逗号分隔
                        if (parts.length >= 2) {
                            string word = parts[0].trim(); //要求至少包含单词和中文释义
                            string meaning = parts[1].trim(); //获取单词
                            string example =parts.length>=3 ? parts[2].trim():"", //获取中文释义

                            word = removeQuotes(word) //去掉单词两边的引号
                            meaning = removeQuotes(meaning) //去掉中文释义的两边引号
                            example = removeQuotes(example) //去掉例句两边的引号
                                
                            if (!word.isEmpty() && !meaning.isEmpty()) { //无单词 无中文释义
                                Word newWord = new Word(word, meaning, example); //创建新词（单词中文释义例句）
                                currentWordList.addWordWithoutSave(newWord); //添加不立即保存
                                importCount++; //算入数量
                            }
                        }
                    }
                }

                currentWordList.saveToFile(); //保存文件
                loadWordData(); //刷新表格
                JOptionPane.showMessageDialog(this, "成功导入 " + importCount + " 个单词！", "导入完成", JOptionPane.INFORMATION_MESSAGE); //提示成功导入数量
            } catch (IOException e) { //导入时出现错误
                JOptionPane.showMessageDialog(this, "导入失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE); //提示导入失败
                e.printStackTrace(); //显示信息
            }
        }
    }

    private void resetStudyStats() { //重置学习统计
        totalStudied = 0; //学习单词总量0
        correctCount = 0; //正确总数0
        wrongCount = 0; //错误总数0
        if (currentWordLabel != null) {
            currentWordLabel.setForeground(UIManager.getColor("Label.foreground")); //重置后文本颜色为绿色
        }
        updateStudyStats(); //更新数据
    }

    private void updateStudyStats() { //更新学习统计
        totalWordsLabel.setText(String.valueOf(currentWordList.size())); //总单词
        correctCountLabel.setText(String.valueOf(correctCount)); //正确数
        wrongCountLabel.setText(String.valueOf(wrongCount)); //错误数
    }

    private void loadNextWord() { //加载下一个单词
        List<Word> words = currentWordList.getAllWords(); //单词中所有单词
        if (words.isEmpty()) { //单词为0
            JOptionPane.showMessageDialog(this, "词典中没有单词！", "提示", JOptionPane.INFORMATION_MESSAGE); //提示
            return;
        }

        int index = (int) (Math.random() * words.size());
        currentWord = words.get(index); //随机选择一个单词

        currentWordLabel.setText(currentWord.getWord()); //更新界面
        currentWordLabel.setForeground(UIManager.getColor("Label.foreground")); //重置文本为绿色
        meaningLabel.setText("含义: " + currentWord.getMeaning()); //正确单词中文释义
        exampleLabel.setText("例句: " + currentWord.getExample()); //例句

        meaningLabel.setVisible(false); //不显示含义
        exampleLabel.setVisible(false); //不显示例句

        answerField.setText(""); //清空输入框
        answerField.requestFocus(); //获取新焦点

        nextButton.setEnabled(false); //禁用下一个按钮
    }

    private void checkAnswer() { //检查答案
        String answer = answerField.getText().trim(); //输入答案后去除空格
        if (answer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入答案！", "提示", JOptionPane.INFORMATION_MESSAGE); //没输入 提示
            return;
        }
        
        totalStudied++; //学习次数增加

        meaningLabel.setVisible(true); //显示中文释义
        exampleLabel.setVisible(true); //显示例句

        if (answer.equalsIgnoreCase(currentWord.getMeaning())) { //// 检查答案 答对
            correctCount++; //正确数增加
            currentWordLabel.setForeground(Color.GREEN); //显示为绿色
            currentWordList.recordReview(currentWord); //正确数量增加
        } else { // 答错
            wrongCount++; //错误数增加
            currentWordLabel.setForeground(Color.RED); //显示为红色
            currentWordList.recordWrong(currentWord); //加入错题本
        }

        updateStudyStats(); //更新统计

        nextButton.setEnabled(true); //启用下一个按钮
    }

    private void showStudyResult() { //显示学习结果
        if (totalStudied > 0) { //已学习
            String message = String.format(
                "学习统计:\n\n已学习单词: %d\n答对: %d\n答错: %d\n正确率: %.1f%%",
                totalStudied, correctCount, wrongCount, 
                (double) correctCount / totalStudied * 100 //学习内容正确率
            );
            JOptionPane.showMessageDialog(this, message, "学习结果", JOptionPane.INFORMATION_MESSAGE); //弹窗显示结果
        }
    }

    private void loadWrongWords() { //加载错题列表
        wrongTableModel.setRowCount(0);  // 清空表格
        
        List<Word> wrongWords = currentWordList.getWrongWordsList(); //获取错题
        for (Word word : wrongWords) {
            Object[] rowData = {
                word.getWord(), 
                word.getMeaning(),
                word.getExample(),
                word.getWrongCount(),
                word.getReviewCount()
            };
            wrongTableModel.addRow(rowData); //错题（包括以上单词中文释义例句错误数量复习情况信息）加入表格
        }
    }

    private void exportWrongWords() { //导出错题
        List<Word> wrongWords = currentWordList.getWrongWordsList(); //获取错题
        if (wrongWords.isEmpty()) { //无错题
            JOptionPane.showMessageDialog(this, "没有错题可导出！", "提示", JOptionPane.INFORMATION_MESSAGE); //提示无导出
            return;
        }
        
        String exportName = JOptionPane.showInputDialog(this, "请输入导出文件名（不含.csv）:", "导出错题", JOptionPane.QUESTION_MESSAGE); //弹窗输入文件名
        if (exportName != null && !exportName.trim().isEmpty()) {
            String fileName = exportName.trim() + "_错题.csv"; //形成文件名
            WordList exportList = new WordList(fileName); //创建新文件
            
            for (Word word : wrongWords) {
                exportList.addWord(word); //错题添加文件中
            }
            
            JOptionPane.showMessageDialog(this, "错题已导出到文件: " + fileName, "导出成功", JOptionPane.INFORMATION_MESSAGE); //弹出成功窗口
        }
    }
 
    private void removeFromWrongList() { //从错题本中移除单词
        int selectedRow = wrongTable.getSelectedRow(); //获得错题
        if (selectedRow == -1) { //没选择
            JOptionPane.showMessageDialog(this, "请选择要移除的单词！", "提示", JOptionPane.INFORMATION_MESSAGE); //提示
            return;
        }
        
        String wordText = (String) wrongTable.getValueAt(selectedRow, 0); //选取单词
    
        List<Word> words = currentWordList.getAllWords(); //查找对应的单词
        for (Word word : words) {
            if (word.getWord().equals(wordText)) {
                word.setWrongCount(0); //改单词错误数0
                currentWordList.saveToFile(); //保存更改
                saveConfig(); //保存当前词典
                loadWrongWords(); //刷新
                return;
            }
        }
    }

    private void deleteDictionary() { //删除字典
        if ((dictComboBox.getSelectedItem() == null) { //没有选择字典
            JOptionPane.showMessageDialog(this, "请先选择一个词典！", "提示", JOptionPane.WARNING_MESSAGE); //弹出提示
            return;
        }

        String dictName = dictComboBox.getSelectedItem().toString(); //获取选中的单词词典的名称（单词表
        int confirm = JOptionPane.showConfirmDialog(this, 
            "确定要删除词典 '" + dictName + "' 吗？此操作不可恢复！", //弹出提示
            "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //操作不可删除
        if (confirm == JOptionPane.YES_OPTION) { //确定
            File dictFile = new File(dictName);
            if (dictFile.exists() && dictFile.delete()) { //存在且删除成功
                JOptionPane.showMessageDialog(this, "词典已成功删除！", "成功", JOptionPane.INFORMATION_MESSAGE); //提示

                loadDictionaries(); //重新加载
        
                if (dictComboBox.getItemCount() > 0) { //有剩余字典
                    dictComboBox.setSelectedIndex(0); //选择第一个字典
                    currentDictPath = dictComboBox.getSelectedItem().toString(); //更新当前词典路径
                    currentWordList = new WordList(currentDictPath); //加载内容
                } else {
                    currentDictPath = "words.csv"; //无词典创建新的
                    currentWordList = new WordList(currentDictPath); 
                    currentWordList.saveToFile(); //保存
                    loadDictionaries(); //加载新的
                }

                loadWordData(); //加载词表到数据中
            } else {
                JOptionPane.showMessageDialog(this, "删除词典失败！", "错误", JOptionPane.ERROR_MESSAGE); //失败弹出数据
            }
        }
    }

    private void batchDeleteWords() { //批量删除单词
        int[] selectedRows = wordTable.getSelectedRows(); //获取所选
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的单词！", "提示", JOptionPane.INFORMATION_MESSAGE); //提示
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "确定要删除选中的 " + selectedRows.length + " 个单词吗？", 
            "确认删除", JOptionPane.YES_NO_OPTION); //弹出提示

        if (confirm == JOptionPane.YES_OPTION) { //同意
            List<Word> wordsToDelete = new ArrayList<>(); //创建新列表用于删除单词
            for (int i = selectedRows.length - 1; i >= 0; i--) { //倒序
                Word word = currentWordList.getWord(selectedRows[i]); //获取单词
                if (word != null) {
                    wordsToDelete.add(word); //添加到列表
                }
            }

            for (Word word : wordsToDelete) { //获取单词
                currentWordList.removeWord(word); //删除
            }

            loadWordData(); //刷新
             JOptionPane.showMessageDialog(this, "已删除 " + wordsToDelete.size() + " 个单词！", "删除完成", JOptionPane.INFORMATION_MESSAGE); //弹出提示
        }
    }
                   
    public static void main(String[] args) { //使用系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(); //打开失败 弹出信息
        }

        SwingUtilities.invokeLater(new Runnable() { //启动应用
            @Override
            public void run() {
                new MainWindow().setVisible(true); //显示主页面
            }
        });
    }

    private string removeQuotes(string str) //移除两边的引号
        if (str.  == null || str. length() < 2) //不需移除
            return str;
        }

        if ((str.startsWith("\") && str.endsWith("\")) || 
            (str.startsWith("\") && str.endsWith(""))) { //支持中文的引号
            return str.substring(1, str.length() - 1); //处理后
        }

        return str //不满足 返回

    }
}
