package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Properties;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.border.AbstractBorder;

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
    private JTextField wordField;//单词文本框
    private JTextField meaningField; //释义文本框
    private JTextField exampleField; //例句文本框
    private JPanel studyPanel; //学习面板
    private JLabel titleLabel; //标签标题
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
    private boolean isReviewMode = false; //是否为复习模式 
    private JPanel wrongPanel; //错题版主页面
    private JTable wrongTable; //错题表格
    private DefaultTableModel wrongTableModel; //错误表格数据
    private JComboBox<String> wrongDictComboBox; // 错题本下拉选择框
    private JLabel feedbackLabel; //反馈
    private Timer feedbackTimer; //动画
    private float feedbackAlpha = 1.0f; //透明度

    public MainWindow() { // 设置窗口标题和大小
        super("单词学习系统"); //标题
        setSize(800, 600); //窗口尺寸
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //退出
        setLocationRelativeTo(null); //窗口居中显示

        Font globalFont = new Font("Dialog", Font.PLAIN, 12); //设置UI字体，支持法语
        UIManager.put("Button.Font", globalFont);
        UIManager.put("Label.Font", globalFont);
        UIManager.put("TextField.Font", globalFont);
        UIManager.put("TextArea.Font", globalFont);
        UIManager.put("Table.Font", globalFont);
        UIManager.put("ComboBox.Font", globalFont);

        loadConfig(); //加载配置
        
        currentWordList = new WordList(currentDictPath); // 初始化单词列表
        
        initComponents(); // 初始化界面组件

        loadDictionaries(); //加载所有字典
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
            showDictionarySelectionDialog(); //弹出窗口
        });

        JMenuItem reviewItem = new JMenuItem("单词复习"); 
        reviewItem.addActionListener(e -> {
            loadWrongWords();
            showReviewDictionarySelectionDialog(); //弹出窗口选择词表进行复习
        });
        
        JMenuItem wrongItem = new JMenuItem(WRONG_PANEL);
        wrongItem.addActionListener(e -> {
            loadWrongWords(); //错题本加载
            cardLayout.show(cardPanel, WRONG_PANEL); //切换到错题本页面
        });
        
        pageMenu.add(homeItem); //主页添加到页面菜单
        pageMenu.add(dictItem); //词典添加到页面菜单
        pageMenu.add(studyItem); //学习添加到页面菜单
        pageMenu.add(reviewItem); //学习添加到页面菜单
        pageMenu.add(wrongItem); //错题本添加到页面菜单  
        menuBar.add(fileMenu);
        menuBar.add(pageMenu); // // 添加菜单到菜单栏
        
        setJMenuBar(menuBar);  // 设置菜单栏
    }
 
    private void createHomePanel() {
        homePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //自定义面板实现渐变背景

                GradientPaint gradient = new GradientPaint(
                     0, 0, new Color(240, 248, 255), 
                     0, getHeight(), new Color(176, 224, 230) // Powder Blue//创建渐变由粉到蓝
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight()); //渐变色 矩形背景
                g2d.dispose(); //释放
            }
        };

        JPanel titlePanel = new JPanel(new BorderLayout()); //创建面板
        titlePanel.setOpaque(false); //背景透明
        titlePanel.setBorder(new EmptyBorder(30, 20, 20, 20)); //边距
        
        JLabel titleLabel = new JLabel("单词学习系统", JLabel.CENTER); //顶部标题
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 42)); //字体大小
        titleLabel.setForeground(new Color(25, 25, 112)); //颜色
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0)); //边距
        
        JLabel subtitleLabel = new JLabel("提升您的词汇量，轻松学习新单词", JLabel.CENTER); //副标题
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18)); 
        subtitleLabel.setForeground(new Color(70, 130, 180)); 

        titlePanel.add(titleLabel, BorderLayout.NORTH); //主标题添加到标题的面板顶部
        titlePanel.add(subtitleLabel, BorderLayout.CENTER); //副标题添加到标题的面板

        homePanel.add(titlePanel, BorderLayout.NORTH); //标题面板到主面板

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 30, 30)); //创建面板
        buttonPanel.setOpaque(false); //透明背景
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 80, 100)); //内边距

        JButton dictButton = createStyledButton("词表管理", new Color(65, 105, 225)); //创建词表管理按钮
        dictButton.addActionListener(e -> {
            loadDictionaries(); //加载词库
            loadWordData(); //加载单词
            cardLayout.show(cardPanel, DICT_PANEL); //切换词库页面
        });

        JButton studyButton = createStyledButton("单词学习", new Color(46, 139, 87)); //创建单词学习按钮
        studyButton.addActionListener(e -> {
            showDictionarySelectionDialog(); //弹出选择词表的窗口
        });

        JButton reviewButton = createStyledButton("单词复习", new Color(205, 92, 92)); //创建单词复习按钮
        reviewButton.addActionListener(e -> {
            showReviewDictionarySelectionDialog(); //弹出选择窗口
        });

        JButton wrongButton = createStyledButton("错题本", new Color(218, 165, 32)); //创建错题本按钮
        wrongButton.addActionListener(e -> { 
            loadWrongWords(); //加载错题本
            cardLayout.show(cardPanel, WRONG_PANEL); //切换错题本页面
        });
        
        buttonPanel.add(dictButton); //添加词表管理按钮
        buttonPanel.add(studyButton); //添加单词学习按钮
        buttonPanel.add(reviewButton); //添加复习按钮
        buttonPanel.add(wrongButton); //添加错题本按钮
        
        homePanel.add(buttonPanel, BorderLayout.CENTER); //以上按钮添加至主页
        
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //自定义渐变的圆角按钮的构建

                GradientPaint gradient = new GradientPaint(
                    0, 0, baseColor.brighter(),
                    0, getHeight(), baseColor //垂直的渐变色
                );
                g2d.setPaint(gradient);

                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20)); //圆角的矩形

                g2d.setColor(baseColor.darker());
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20)); //边框的绘制

                g2d.dispose();

                super.paintComponent(g); //文本颜色和位置
            }
        };

        button.setFont(new Font("微软雅黑", Font.BOLD, 24)); //按钮字体大小
        button.setForeground(Color.WHITE); //颜色
        button.setFocusPainted(false); //移除焦点边框
        button.setBorderPainted(false); //不绘制默认边框
        button.setContentAreaFilled(false); //不填充内容区域
        button.setOpaque(false); //透明
        
        button.setPreferredSize(new Dimension(200, 120)); //按钮尺寸
        
        button.addMouseListener(new java.awt.event.MouseAdapter() { 
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); //悬停效果 光标变手型
            }
        });

        return button;
    }
                
    private void createDictPanel() { //创建词表管理界面
        dictPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //自定义面板实现渐变

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 248, 255),
                    0, getHeight(), new Color(176, 224, 230) //顶部到底部渐变
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        dictPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); //边距
        
        JPanel topPanel = new JPanel(new BorderLayout()); //顶部面板
        topPanel.setOpaque(false); //透明背景

        JLabel titleLabel = new JLabel("词表管理", JLabel.CENTER); //词表标题标签
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28)); //字体大小
        titleLabel.setForeground(new Color(25, 25, 112)); //颜色
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0)); //边距
        topPanel.add(titleLabel, BorderLayout.NORTH); //添加至顶部
        
        JPanel dictSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); //词典选择面板
        dictSelectPanel.setOpaque(false); //透明背景
        
        JLabel selectLabel = new JLabel("选择词典:"); //创建标签
        selectLabel.setFont(new Font("微软雅黑", Font.BOLD, 14)); //字体大小
        selectLabel.setForeground(new Color(70, 130, 180)); //颜色
        dictSelectPanel.add(selectLabel); //添加到面板
        
        dictComboBox = new JComboBox<>(); //创建下拉框选择词典
        dictComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14)); //字体大小
        dictComboBox.setPreferredSize(new Dimension(220, 30)); //尺寸
        dictComboBox.setBackground(Color.WHITE); //颜色
        dictComboBox.setUI(new BasicComboBoxUI());
        dictComboBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(176, 224, 230), 1),
            new EmptyBorder(2, 5, 2, 5)

        ));
        dictComboBox.addActionListener(e -> {
            if (dictComboBox.getSelectedItem() != null) {
                currentDictPath = dictComboBox.getSelectedItem().toString();
                currentWordList = new WordList(currentDictPath);
                saveConfig(); //保存配置
                loadWordData();
            }
        });
        dictSelectPanel.add(dictComboBox); //左侧区域添加下拉框
        
        JButton newDictButton = createSmallButton("新建词典", new Color(65, 105, 225)); //新建按钮
        newDictButton.addActionListener(e -> createNewDictionary()); //调用新建词典
        dictSelectPanel.add(newDictButton);

        JButton deleteDictButton = createSmallButton("删除词典", new Color(205, 92, 92)); //删除字典按钮
        deleteDictButton.addActionListener(e -> deleteDictionary()); //调用删除字典
        dictSelectPanel.add(deleteDictButton); //按钮谈驾到词典选择的面板上
        
        topPanel.add(dictSelectPanel, BorderLayout.WEST); //左侧词典选择区域加入顶部面板

        JButton homeButton = createSmallButton("返回主页", new Color(70, 130, 180)); //返回主页按钮
        homeButton.addActionListener(e -> cardLayout.show(cardPanel, HOME_PANEL)); //切换主页

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); //创建面板右对齐
        rightPanel.setOpaque(false); //透明背景
        rightPanel.add(homeButton); //首页按钮添加到面板
        topPanel.add(rightPanel, BorderLayout.EAST); //右面板添加到右边
        
        dictPanel.add(topPanel, BorderLayout.NORTH); //顶部加入到词典主页面

        JPanel tablePanel = new JPanel(new BorderLayout()); //创建新面板
        tablePanel.setOpaque(false); //透明背景
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); //边距

        JLabel tableTitle = new JLabel("词表内容", JLabel.LEFT); //标题 
        tableTitle.setFont(new Font("微软雅黑", Font.BOLD, 16)); //字体
        tableTitle.setForeground(new Color(25, 25, 112)); //颜色
        tablePanel.add(tableTitle, BorderLayout.NORTH); //添加到顶部面板
        
        String[] columnNames = {"单词", "含义", "例句",}; //创建表格
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 表格不可编辑
            }
        };
        wordTable = new JTable(tableModel); //创建表格
        wordTable.setFont(new Font("微软雅黑", Font.PLAIN, 14)); //字体
        wordTable.setRowHeight(30);
        wordTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //允许多选

        JTableHeader header = wordTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 14)); //字体
        header.setBackground(new Color(65, 105, 225)); //颜色
        header.setPreferredSize(new Dimension(header.getWidth(), 35)); //背景颜色
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("微软雅黑", Font.BOLD, 14));
                c.setForeground(Color.WHITE);
                c.setBackground(new Color(65, 105, 225));
                setBorder(new EmptyBorder(5, 0, 5, 0));
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        }; //表头渲染

        for (int i = 0; i < wordTable.getColumnCount(); i++) {
            wordTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        } //表头渲染器

        wordTable.setShowGrid(true);
        wordTable.setGridColor(new Color(230, 230, 250));
        wordTable.setSelectionBackground(new Color(65, 105, 225, 50));
        wordTable.setSelectionForeground(new Color(25, 25, 112));
        wordTable.setIntercellSpacing(new Dimension(5, 5)); //单元格美化

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    c.setBackground(new Color(65, 105, 225, 50));
                    c.setForeground(new Color(25, 25, 112));
                } else {// 交替行颜色
                    if (row % 2 == 0) {
                        c.setBackground(new Color(240, 248, 255)); 
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    c.setForeground(new Color(70, 70, 70));
                }
                setBorder(new CompoundBorder(getBorder(), new EmptyBorder(5, 10, 5, 10)));
                
                return c;
            }
        };

        centerRenderer.setHorizontalAlignment(JLabel.LEFT);

        for (int i = 0; i < wordTable.getColumnCount(); i++) {
            wordTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        wordTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        wordTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        wordTable.getColumnModel().getColumn(2).setPreferredWidth(350); //表格设置

        JScrollPane scrollPane = new JScrollPane(wordTable); //滚动功能
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(176, 224, 230), 1)); //颜色边框
        scrollPane.getViewport().setBackground(Color.WHITE); //颜色
        tablePanel.add(scrollPane, BorderLayout.CENTER); //添加至中心位置

        dictPanel.add(tablePanel, BorderLayout.CENTER); //添加到中心位置

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10)); //底部面板
        bottomPanel.setOpaque(false); //透明背景

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10)); //面板
        inputPanel.setOpaque(false); //透明
        inputPanel.setBorder(BorderFactory.createTitledBorder( //边框
            BorderFactory.createLineBorder(new Color(70, 130, 180), 1), //设置
            "添加/编辑单词",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, //对齐方式
            javax.swing.border.TitledBorder.DEFAULT_POSITION, //位置
            new Font("微软雅黑", Font.BOLD, 14), //字体
            new Color(25, 25,112) //颜色
        ));

        JLabel wordLabel = new JLabel("单词:"); //标签
        wordLabel.setFont(new Font("微软雅黑", Font.BOLD, 14)); //字体
        wordLabel.setForeground(new Color(70, 130, 180)); //颜色
        inputPanel.add(wordLabel); //添加到面板中

        wordField = new JTextField();  //文本框
        wordField.setFont(new Font("微软雅黑", Font.PLAIN, 14)); //字体
        wordField.setBorder(new RoundedBorder(new Color(176, 224, 230), 1, 10));
        wordField.setMargin(new Insets(5, 10, 5, 10));
        inputPanel.add(wordField);

        JLabel meaningLabel = new JLabel("含义:");
        meaningLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        meaningLabel.setForeground(new Color(70, 130, 180)); 
        inputPanel.add(meaningLabel);

        meaningField = new JTextField();
        meaningField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        meaningField.setBorder(new RoundedBorder(new Color(176, 224, 230), 1, 10));
        meaningField.setMargin(new Insets(5, 10, 5, 10));
        inputPanel.add(meaningField);

        JLabel exampleLabel = new JLabel("例句:");
        exampleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exampleLabel.setForeground(new Color(70, 130, 180));
        inputPanel.add(exampleLabel);

        exampleField = new JTextField();
        exampleField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        exampleField.setBorder(new RoundedBorder(new Color(176, 224, 230), 1, 10));
        exampleField.setMargin(new Insets(5, 10, 5, 10));
        inputPanel.add(exampleField); //同单词板块一样 由于时间紧迫 不再赘述
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); //按钮面板
        buttonPanel.setOpaque(false); //透明

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

        bottomPanel.add(inputPanel, BorderLayout.CENTER); //单词中文释义例句放在面板底部居中
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);  //添加编辑删除放在底部区域

        dictPanel.add(bottomPanel, BorderLayout.SOUTH); //底部面板添加到词典管理底部
    }

    private JButton createSmallButton(String text, Color baseColor) { //创建按钮
    JButton button = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //自定义绘制图形

            GradientPaint gradient = new GradientPaint(
                0, 0, baseColor.brighter(),
                0, getHeight(), baseColor //颜色
            );
            g2d.setPaint(gradient);

            g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

            g2d.setColor(baseColor.darker());
            g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15)); //绘制

            g2d.dispose(); //释放资源

            super.paintComponent(g);
        }
    };

    button.setFont(new Font("微软雅黑", Font.BOLD, 14)); //字体
    button.setForeground(Color.WHITE); //颜色
    button.setFocusPainted(false); //禁用
    button.setBorderPainted(false); //禁用按钮默认边框
    button.setContentAreaFilled(false); //不使用默认填充
    button.setOpaque(false); //透明

    button.setPreferredSize(new Dimension(100, 30)); //按钮尺寸
    
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
             button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    });

        return button;
}

    private void createStudyPanel() { //创建学习页面
        studyPanel = new JPanel(new BorderLayout(10, 10)) { //新页面面板设置
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 248, 255), 
                    0, getHeight(), new Color(176, 224, 230) 
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        studyPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); //基本框架与之前类似 时间紧迫 不过多赘述

        titleLabel = new JLabel("单词学习", JLabel.CENTER); //创建单词学习标签
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(25, 25, 112)); 
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        studyPanel.add(titleLabel, BorderLayout.PAGE_START); //添加到学习面板顶部

        JPanel topPanel = new JPanel(new BorderLayout()); //创建顶部面板
        topPanel.setOpaque(false); //透明背景

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statsPanel.setOpaque(false); 
 
        JLabel totalLabel = new JLabel("总单词数:"); //进行面板创建
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalLabel.setForeground(new Color(70, 130, 180));
        statsPanel.add(totalLabel); //标签添加到面板中
        
        totalWordsLabel = new JLabel("0"); //创建标签
        totalWordsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalWordsLabel.setForeground(new Color(25, 25, 112));
        statsPanel.add(totalWordsLabel); //同上 时间紧迫 不再赘述
        
        JLabel studiedLabel = new JLabel("    已学习:");
        studiedLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        studiedLabel.setForeground(new Color(70, 130, 180));
        statsPanel.add(studiedLabel);
        
        JLabel studiedCountLabel = new JLabel("0");
        studiedCountLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        studiedCountLabel.setForeground(new Color(25, 25, 112)); 
        statsPanel.add(studiedCountLabel); //同上 时间紧迫 不再赘述
        
        JLabel correctLabel = new JLabel("    答对:");
        correctLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        correctLabel.setForeground(new Color(46, 139, 87)); 
        statsPanel.add(correctLabel);
        
        correctCountLabel = new JLabel("0");
        correctCountLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        correctCountLabel.setForeground(new Color(46, 139, 87)); 
        statsPanel.add(correctCountLabel); //同上
        
        JLabel wrongLabel = new JLabel("    答错:");
        wrongLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        wrongLabel.setForeground(new Color(205, 92, 92));
        statsPanel.add(wrongLabel); //同上
        
        wrongCountLabel = new JLabel("0");
        wrongCountLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        wrongCountLabel.setForeground(new Color(205, 92, 92));
        statsPanel.add(wrongCountLabel);
        
        topPanel.add(statsPanel, BorderLayout.WEST); //将统计信息放置顶部左侧

        JButton homeButton = createSmallButton("退出学习", new Color(70, 130, 180));
        homeButton.addActionListener(e -> {
            showStudyResult(); //当前学习结果
            cardLayout.show(cardPanel, HOME_PANEL); //转到主页
        });
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); //创建右侧面板
        rightPanel.setOpaque(false);
        rightPanel.add(homeButton); //主页按钮添加至右侧面板
        topPanel.add(rightPanel, BorderLayout.EAST); //主页按钮位于顶部面板右侧
        
        studyPanel.add(topPanel, BorderLayout.NORTH); //顶部的面板添加到学习页面的顶部

        JPanel centerPanel = new JPanel() { //中间面板
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                g2d.setColor(new Color(176, 224, 230));
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                
                g2d.dispose(); //同类似
            }
        };
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        currentWordLabel = new JLabel("", JLabel.CENTER); //创建空标签，居中
        currentWordLabel.setFont(new Font("微软雅黑", Font.BOLD, 42));
        currentWordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(currentWordLabel); //添加到中间面板
        
        centerPanel.add(Box.createVerticalStrut(30)); //垂直间隔30像素

        meaningLabel = new JLabel("", JLabel.CENTER); //单词含义初始为空
        meaningLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        meaningLabel.setAlignmentX(Component.CENTER_ALIGNMENT); //居中对齐
        meaningLabel.setVisible(false); //初始不可见
        centerPanel.add(meaningLabel); //添加到面板中
        
        centerPanel.add(Box.createVerticalStrut(15)); //15像素间隔

        exampleLabel = new JLabel("", JLabel.CENTER); //显示例句标签
        exampleLabel.setFont(new Font("微软雅黑", Font.ITALIC, 18));
        exampleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); //居中
        exampleLabel.setVisible(false); //初始不可见
        centerPanel.add(exampleLabel); //添加到面板中
        
        centerPanel.add(Box.createVerticalStrut(40));

        JPanel feedbackPanel = new JPanel();
        feedbackPanel.setLayout(new BorderLayout());
        feedbackPanel.setOpaque(false);
        feedbackPanel.setPreferredSize(new Dimension(200, 50));
        feedbackPanel.setMaximumSize(new Dimension(200, 50));
        feedbackPanel.setAlignmentX(Component.CENTER_ALIGNMENT); //创建浮动面板

        feedbackLabel = new JLabel("", JLabel.CENTER);
        feedbackLabel.setFont(new Font("Dialog", Font.BOLD, 36)); // 减小字体大小
        feedbackLabel.setVisible(false);
        feedbackPanel.add(feedbackLabel, BorderLayout.CENTER); //答对反馈
        
        centerPanel.add(feedbackPanel);
        
        centerPanel.add(Box.createVerticalStrut(15));

        JPanel answerPanel = new JPanel(); //创建答案输入子面板
        answerPanel.setLayout(new BoxLayout(answerPanel, BoxLayout.X_AXIS)); //水平排列
        answerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        answerPanel.setOpaque(false);
        
        JLabel answerLabel = new JLabel("输入含义: ");
        answerLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        answerLabel.setForeground(new Color(70, 130, 180));
        answerPanel.add(answerLabel);
        
        answerField = new JTextField(20);
        answerField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        answerField.setMaximumSize(new Dimension(300, 35));
        answerField.setBorder(new RoundedBorder(new Color(176, 224, 230), 1, 15));
        answerField.setMargin(new Insets(5, 10, 5, 10));
        answerPanel.add(answerField);
        
        answerPanel.add(Box.createHorizontalStrut(10));
        
        JButton checkButton = createSmallButton("检查", new Color(46, 139, 87)); 
        checkButton.addActionListener(e -> checkAnswer());
        answerPanel.add(checkButton);
        
        centerPanel.add(answerPanel); //答题区域添加到中间面板
        
        studyPanel.add(centerPanel, BorderLayout.CENTER); //中间面板添加到学习页面中部

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)); 
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        nextButton = createSmallButton("下一个", new Color(65, 105, 225));  //创建下一个按钮
        nextButton.setEnabled(false); //初始不可点击
        nextButton.addActionListener(e -> {
            if (isReviewMode) { //复习模式
                loadNextWrongWord(); //下一个错词
            } else {
                loadNextWord(); //普通单词
            }
            nextButton.setEnabled(false); //再次不可点击，等待新回答
        });
        bottomPanel.add(nextButton); //添加按钮到底部面板
        
        JButton exitButton = createSmallButton("退出学习", new Color(205, 92, 92)); //退出学习按钮
        exitButton.addActionListener(e -> {
            showStudyResult(); //显示学习结果
            cardLayout.show(cardPanel, HOME_PANEL); //返回主页
        });
        bottomPanel.add(exitButton); //添加到面板
        
        studyPanel.add(bottomPanel, BorderLayout.SOUTH); //将底部面板添加到学习页面底部
    }
   
    private void createWrongPanel() { //创建错题本页面
        wrongPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //渐变背景

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 248, 220), // Cornsilk
                    0, getHeight(), new Color(255, 222, 173) //颜色
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        wrongPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); //边距

        JPanel topPanel = new JPanel(new BorderLayout()); //顶部标题面板
        topPanel.setOpaque(false); //透明

        JPanel dictSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); //词典选择的面板
        dictSelectPanel.setOpaque(false); //背景透明

        JLabel selectLabel = new JLabel("选择词典:"); //创建标签
        selectLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        selectLabel.setForeground(new Color(160, 82, 45));
         dictSelectPanel.add(selectLabel);


        JLabel titleLabel = new JLabel("错题本", JLabel.CENTER); //创建标签
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(139, 69, 19)); // 颜色
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0)); //边距
        topPanel.add(titleLabel, BorderLayout.NORTH); //标签添加到上部
    
        wrongDictComboBox = new JComboBox<>(); //下拉框
        wrongDictComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        wrongDictComboBox.setPreferredSize(new Dimension(220, 30)); //尺寸
        wrongDictComboBox.setBackground(Color.WHITE);
        wrongDictComboBox.addActionListener(e -> {
            if (wrongDictComboBox.getSelectedItem() != null) {
                currentDictPath = wrongDictComboBox.getSelectedItem().toString(); //获取字典并更新
                currentWordList = new WordList(currentDictPath); //根据所选创建新文件
                saveConfig(); //保存
                loadWrongWords(); //加载
            }
        });
        dictSelectPanel.add(wrongDictComboBox); //下拉框添加至面板
        
        topPanel.add(dictSelectPanel, BorderLayout.WEST);
        
        JButton homeButton = createSmallButton("返回主页", new Color(205, 133, 63)); //创建返回主页按钮
        homeButton.addActionListener(e -> cardLayout.show(cardPanel, HOME_PANEL)); //返回首页
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(homeButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        wrongPanel.add(topPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel tableTitle = new JLabel("错题列表", JLabel.LEFT);
        tableTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        tableTitle.setForeground(new Color(139, 69, 19));
        tablePanel.add(tableTitle, BorderLayout.NORTH); //原因同上

        String[] columnNames = {"单词", "含义", "例句", "答错次数", "复习次数"}; //创建表格
        wrongTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  //表格不可编辑
            }
        };
        
        wrongTable = new JTable(wrongTableModel); //创建表格
        wrongTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        wrongTable.setRowHeight(25);
        wrongTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //单项选择模式
        wrongTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        wrongTable.getTableHeader().setBackground(new Color(255, 248, 220));
        wrongTable.setSelectionBackground(new Color(255, 222, 173, 100));
        wrongTable.setGridColor(new Color(222, 184, 135));

        wrongTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        wrongTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        wrongTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        wrongTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        wrongTable.getColumnModel().getColumn(4).setPreferredWidth(80); //以上为宽度
        
        JScrollPane scrollPane = new JScrollPane(wrongTable); //添加滚条
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 184, 135), 1));
        wrongPanel.add(scrollPane, BorderLayout.CENTER); //添加表格到页面中样

    
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); //按钮面板
        buttonPanel.setOpaque(false); //透明
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); //顶部边距为10

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //创建面板
        statsPanel.setOpaque(false); //透明

        JLabel statsLabel = new JLabel("错题统计: "); //错题统计面板
        statsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statsLabel.setForeground(new Color(139, 69, 19));
        statsPanel.add(statsLabel); //添加到统计面板中

        JLabel countLabel = new JLabel("0 个单词"); //初始值为零0
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statsPanel.add(countLabel);

        Runnable updateStats = () -> {
            int count = wrongTableModel.getRowCount();
            countLabel.setText(count + " 个单词"); //单词数
        };

        updateStats.run();

        JButton exportButton = createSmallButton("导出错题", new Color(210, 105, 30)); //导出错题按钮
        exportButton.addActionListener(e -> {
            exportWrongWords();
            updateStats.run(); //重新统计
        });   
        
        JButton removeButton = createSmallButton("移出错题本", new Color(205, 92, 92)); //移除错题本按钮
        removeButton.addActionListener(e -> {
            removeFromWrongList();
            updateStats.run();
        });

        JButton studyButton = createSmallButton("复习错题", new Color(46, 139, 87)); //复习错题按钮
        studyButton.addActionListener(e -> {
            if (wrongTableModel.getRowCount() > 0) { //有错题
                isReviewMode = true; //启用
                resetStudyStats(); //重置数据
                loadNextWrongWord(); //加载数据
                titleLabel.setText("单词复习模式 - " + currentDictPath); //修改标题
                cardLayout.show(cardPanel, STUDY_PANEL); //切换学习界面
            } else {
                JOptionPane.showMessageDialog(this, "当前没有错题可复习！", "提示", JOptionPane.INFORMATION_MESSAGE); //弹出窗口
            }
        });

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); //创建面板
        rightButtonPanel.setOpaque(false); //背景透明
        rightButtonPanel.add(studyButton); //按钮放在右侧面板中
        rightButtonPanel.add(exportButton); //添加导出错题按钮
        rightButtonPanel.add(removeButton); //添加删除错题按钮

        JPanel bottomPanel = new JPanel(new BorderLayout()); //创建底部面板
        bottomPanel.setOpaque(false);
        bottomPanel.add(statsPanel, BorderLayout.WEST); //统计面板放在左侧面板
        bottomPanel.add(rightButtonPanel, BorderLayout.EAST); //按钮面板放在右侧

        wrongPanel.add(bottomPanel, BorderLayout.SOUTH); //整个按钮面板放早底部
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
        System.out.println("loading");
        tableModel.setRowCount(0);  // 清空表格
        
        List<Word> words = currentWordList.getAllWords(); //获取当前词典中单词
        for (Word word : words) {
            Object[] rowData = {
                word.getWord(), //单词
                word.getMeaning(), //中文释义
                word.getExample(), //例句
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
                        if(line.trim().isEmpty()){ //空行跳过
                            continue; //继续下一行
                        }

                        String[] parts = line.split(","); //逗号分隔
                        if (parts.length >= 2) {
                            String word = parts[0].trim(); //要求至少包含单词和中文释义
                            String meaning = parts[1].trim(); //获取单词
                            String example =parts.length>=3 ? parts[2].trim():""; //获取中文释义

                            word = removeQuotes(word); //去掉单词两边的引号
                            meaning = removeQuotes(meaning); //去掉中文释义的两边引号
                            example = removeQuotes(example); //去掉例句两边的引号
                                
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
        currentWord = words.get(index);
        
        currentWordLabel.setText(currentWord.getWord()); //设置标签
        currentWordLabel.setForeground(UIManager.getColor("Label.foreground")); //重置文本为绿色
        meaningLabel.setText("含义: " + currentWord.getMeaning()); //正确单词中文释义
        exampleLabel.setText("例句: " + currentWord.getExample()); //例句

        meaningLabel.setVisible(false); //不显示含义
        exampleLabel.setVisible(false); //不显示例句

        answerField.setText(""); //清空输入框
        answerField.requestFocus(); //获取新焦点

        nextButton.setEnabled(false); //禁用下一个按钮
    }
        
    private void loadNextWrongWord() {
        List<Word> wrongWords = currentWordList.getWrongWordsList(); //获取错词
        if (wrongWords.isEmpty()) { //没有错词
            JOptionPane.showMessageDialog(this, "没有错题可复习！", "提示", JOptionPane.INFORMATION_MESSAGE); //弹出提示
            return;
        }

        int index = (int) (Math.random() * wrongWords.size());
        currentWord = wrongWords.get(index); //错词随机

        currentWordLabel.setText(currentWord.getWord()); //更新界面
        currentWordLabel.setForeground(UIManager.getColor("Label.foreground")); //文本颜色重置
        meaningLabel.setText("含义: " + currentWord.getMeaning()); //设置含义
        exampleLabel.setText("例句: " + currentWord.getExample()); //设置例句

        meaningLabel.setVisible(false); //隐藏含义
        exampleLabel.setVisible(false); //银行例句

        answerField.setText(""); //清空输入框
        answerField.requestFocus(); //获取焦点

        nextButton.setEnabled(false); //禁用下一个

        totalWordsLabel.setText(String.valueOf(wrongWords.size())); //统计信息更新
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
                loadWrongWords(); //刷新
                return;
            }
        }
    }

    private void deleteDictionary() { //删除字典
        if (dictComboBox.getSelectedItem() == null) { //没有选择字典
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
                    saveConfig(); //保存当前词典配置
                } else {
                    currentDictPath = "words.csv"; //无词典创建新的
                    currentWordList = new WordList(currentDictPath); 
                    currentWordList.saveToFile(); //保存
                    saveConfig(); //保存当前词典配置
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

    private String removeQuotes(String str){ //移除两边的引号
        if (str == null || str. length() < 2) //不需移除
            return str;

        if ((str.startsWith("\"") && str.endsWith("\"")) || 
            (str.startsWith("'") && str.endsWith("'"))) { //支持双引号和单引号
            return str.substring(1, str.length() - 1); //处理后

        }
        return str; //不满足 返回
    }
            
    private void showDictionarySelectionDialog() { //创建数组
            String[] dictNames = new String[dictComboBox.getItemCount()]; 
            for (int i = 0; i < dictComboBox.getItemCount(); i++) {
                dictNames[i] = dictComboBox.getItemAt(i); //词典名称存入
            }

            String selectedDict = (String) JOptionPane.showInputDialog( //弹出窗口
                this,
                "请选择要学习的词典:", //内容
                "选择词典", //标题
                JOptionPane.QUESTION_MESSAGE, //问号
                null, //默认图标
                dictNames, //可下拉
                currentDictPath //默认选中的词典
            );

            if (selectedDict != null) { //选择了词典
                currentDictPath = selectedDict; //词典路径为被选择的
                currentWordList = new WordList(currentDictPath); //用新保存的词典创建新表
                saveConfig(); //保存当前词典的配置
                
                if (currentWordList.size() > 0) { //检查是否有单词
                    isReviewMode = false; //设置为学习模式
                    resetStudyStats(); //学习统计重置
                    loadNextWord(); //加载第一个单词

                    titleLabel.setText("单词学习模式 - " + currentDictPath); //更新标题
                    
                    cardLayout.show(cardPanel, STUDY_PANEL); //显示学习的版面
                } else {
                    JOptionPane.showMessageDialog(this, "所选词典没有单词，请先添加单词！", "提示", JOptionPane.WARNING_MESSAGE); //无词弹出提示
                }
            }
        }

        private void showReviewDictionarySelectionDialog() { //创建
            String[] dictNames = new String[dictComboBox.getItemCount()]; //保存词典名
            for (int i = 0; i < dictComboBox.getItemCount(); i++) {
                dictNames[i] = dictComboBox.getItemAt(i); //存入名称
            }

            String selectedDict = (String) JOptionPane.showInputDialog(
                this,
                "请选择要复习错题的词典:",
                "选择词典",
                JOptionPane.QUESTION_MESSAGE, //弹出对话框用问号
                null,
                dictNames, //提供词典
                currentDictPath //默认词典
            );

            if (selectedDict != null) { //选择执行
                currentDictPath = selectedDict; //词典路径为所选
                currentWordList = new WordList(currentDictPath); //新词典
                saveConfig(); //保存当前词典配置
                
                List<Word> wrongWords = currentWordList.getWrongWordsList(); //获取错词
                if (wrongWords.size() > 0) { //有错词进入复习
                    isReviewMode = true; //设置为复习模式
                    resetStudyStats(); //学习重置
                    loadNextWrongWord(); //加载错词

                    titleLabel.setText("单词学习模式 - " + currentDictPath); //更新标题
                    
                    cardLayout.show(cardPanel, STUDY_PANEL); //学习面板显示
                } else {
                    JOptionPane.showMessageDialog(this, "所选词典没有错题记录，请先进行单词学习！", "提示", JOptionPane.WARNING_MESSAGE); //弹出提示
                }
            }
        }
        private class RoundedBorder extends AbstractBorder { //圆角边框美化
            private Color color;
            private int thickness;
            private int radius;
            
            public RoundedBorder(Color color, int thickness, int radius) {
                this.color = color;
                this.thickness = thickness;
                this.radius = radius;
            }
            
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(thickness));
                g2d.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
                g2d.dispose();
            }
            
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(thickness + 5, thickness + 5, thickness + 5, thickness + 5);
            }
            
            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        }
        private void saveConfig() { // 保存配置
            Properties props = new Properties();
            props.setProperty("currentDictPath", currentDictPath);
            
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "Word Learning System Configuration");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "保存配置失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    } 
         

