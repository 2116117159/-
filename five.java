
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;


import java.util.Optional;
import java.util.Random;
import java.util.Stack;

public class five extends Application {

    private static final int TILE_SIZE = 40; // 棋盘格子大小
    private static final int WIDTH = 15; // 棋盘宽度
    private static final int HEIGHT = 15; // 棋盘高度

    private Tile[][] board = new Tile[WIDTH][HEIGHT]; // 棋盘格子数组
    private boolean blackTurn = true; // 黑棋先手
    private boolean playerBlack = true; // 玩家执黑棋
    private boolean gameEnded = false; // 游戏是否结束
    private Stack<Move> movesHistory = new Stack<>(); // 棋子移动历史记录
    private Random random = new Random(); // 随机数生成器

    // 创建Pane容器
    Pane boardPane = new Pane();
    boolean hasForbiddenPositions = false; // 添加一个变量来跟踪是否有禁止位置

    // 创建游戏界面
    private Pane createContent() {
        VBox root = new VBox(10); // 垂直布局，设置间距
        boardPane.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

        // 初始化棋盘格子
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y);
                board[x][y] = tile;
                boardPane.getChildren().add(tile);
            }
        }

        // 按钮样式
        String buttonStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #ffffff; -fx-text-fill: #333333;";

        // 开始按钮
        Button btnStart = new Button("开始");
        btnStart.setStyle(buttonStyle + "-fx-text-fill: green;");
        btnStart.setOnAction(e -> startGame());

        // 悔棋按钮
        Button btnUndo = new Button("悔棋");
        btnUndo.setStyle(buttonStyle + "-fx-text-fill: blue;");
        btnUndo.setOnAction(e -> undoMove());

        // 认输按钮
        Button btnResign = new Button("认输");
        btnResign.setStyle(buttonStyle + "-fx-text-fill: red;");
        btnResign.setOnAction(e -> resignGame());



        HBox buttonBox = new HBox(10, btnStart, btnUndo, btnResign); // 按钮水平布局，设置间距
        buttonBox.setAlignment(Pos.CENTER); // 水平居中

        root.getChildren().addAll(buttonBox, boardPane);

        return root;
    }

    // 开始游戏
    private void startGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("选择先手");
        alert.setHeaderText("请选择谁先开始游戏：");
        ButtonType playerFirst = new ButtonType("玩家先");
        ButtonType computerFirst = new ButtonType("电脑先");
        ButtonType js = new ButtonType("禁手");
        alert.getButtonTypes().setAll(playerFirst, computerFirst,js);

        Optional<ButtonType> result = alert.showAndWait();
        resetBoard(); // 清空棋盘并准备新游戏
        if (result.isPresent() && result.get() == computerFirst) {
            // 电脑先手，电脑使用黑棋
            blackTurn = true;
            playerBlack = false;
            computerMove(); // 电脑在中心落子
        } else if(result.get() == playerFirst){
            // 玩家先手，玩家使用黑棋
            blackTurn = true;
            playerBlack = true;
        }else{
            blackTurn = true;
            playerBlack = true;
            jsMove();
        }

    }

    // 重置棋盘
    private void resetBoard() {
        gameEnded = false;
        movesHistory.clear();
        for (Tile[] row : board) {
            for (Tile tile : row) {
                tile.getChildren().removeIf(node -> node instanceof Circle);
                tile.piece = null;
            }
        }
    }

    // 玩家认输
    private void resignGame() {
        gameEnded = true;
        String winner = blackTurn ? "白棋" : "黑棋";
        showGameOverDialog("由于认输，" + winner + " 获胜!");
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle("五子棋游戏");
        primaryStage.show();
        startGame();
    }

    // 棋盘格子类
    private class Tile extends Pane {
        private int x, y;
        private Circle piece;

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;
            setPrefSize(TILE_SIZE, TILE_SIZE);
            Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);
            border.setFill(null);
            border.setStroke(Color.BLACK);
            getChildren().add(border);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);

            setOnMouseClicked(this::handleMouseClick);


        }

        // 处理鼠标点击事件
        private void handleMouseClick(MouseEvent event) {
            if (piece != null || gameEnded) {
                return;
            }
            if (playerBlack) {
                if (blackTurn) {
                    if (isForbiddenMove(x, y)) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("提示");
                        alert.setHeaderText(null);
                        alert.setContentText("黑棋禁手");
                        alert.showAndWait();
                        return;
                    }

                    placePiece(Color.BLACK);
                    if (checkWin(x, y, Color.BLACK)) {
                        gameEnded = true;
                        showGameOverDialog("恭喜你赢了");
                    } else {
                        blackTurn = false;
                        computerMove();
                    }
                }
            } else {
                if (!blackTurn) {
                    placePiece(Color.WHITE);
                    if (checkWin(x, y, Color.WHITE)) {
                        gameEnded = true;
                        showGameOverDialog("恭喜你赢了");
                    } else {
                        blackTurn = true;
                        computerMove();
                    }
                }
            }
            if(detectForbiddenPositions()){
                Circle forbiddenPiece = new Circle(TILE_SIZE * 0.3);
                // 设置禁手棋子的位置
                forbiddenPiece.setCenterX(TILE_SIZE * 0.5);
                forbiddenPiece.setCenterY(TILE_SIZE * 0.5);
                forbiddenPiece.setFill(Color.WHITE); // 设置填充颜色
                forbiddenPiece.setStroke(Color.RED); // 设置边框颜色
                forbiddenPiece.setStrokeWidth(2); // 设置边框宽度
                // 将禁手棋子添加到当前容器的子元素中
                boardPane.getChildren().add(forbiddenPiece);
            }
        }

        // 在格子上放置棋子
        private void placePiece(Color color) {
            piece = new Circle(TILE_SIZE * 0.3);
            piece.setCenterX(TILE_SIZE * 0.5);
            piece.setCenterY(TILE_SIZE * 0.5);
            piece.setFill(color);
            if (color == Color.WHITE) {
                piece.setStroke(Color.BLACK); // 添加色块
            }
            getChildren().add(piece);
            //记录移动历史
            movesHistory.push(new Move(x, y, color == Color.BLACK));
        }
    }

    // 电脑落子
    private void computerMove() {
        if (movesHistory.isEmpty()) {
            // 如果是游戏的第一步，电脑直接在中心落子
            int centerX = WIDTH / 2;
            int centerY = HEIGHT / 2;
            Tile centerTile = board[centerX][centerY];
            centerTile.placePiece(Color.BLACK);
            blackTurn = false; // 转换为玩家的回合
        } else {
            if(detectForbiddenPositions()){
                Circle forbiddenPiece = new Circle(TILE_SIZE * 0.3);
                // 设置禁手棋子的位置
                forbiddenPiece.setCenterX(TILE_SIZE * 0.5);
                forbiddenPiece.setCenterY(TILE_SIZE * 0.5);
                forbiddenPiece.setFill(Color.WHITE); // 设置填充颜色
                forbiddenPiece.setStroke(Color.RED); // 设置边框颜色
                forbiddenPiece.setStrokeWidth(2); // 设置边框宽度
                // 将禁手棋子添加到当前容器的子元素中
                boardPane.getChildren().add(forbiddenPiece);
            }
            int bestScore = 0;
            Tile bestTile = null;
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    Tile tile = board[x][y];
                    if (tile.piece == null) {
                        int score = getScore(x, y, Color.WHITE) + getScore(x, y, Color.BLACK);
                        if (score > bestScore) {
                            bestScore = score;
                            bestTile = tile;
                        }
                    }
                }
            }

            if (playerBlack) {
                if (bestTile != null) {
                    bestTile.placePiece(Color.WHITE);
                    if (checkWin(bestTile.x, bestTile.y, Color.WHITE)) {
                        gameEnded = true;
                        showGameOverDialog("很遗憾电脑赢了");
                    } else {
                        blackTurn = playerBlack;
                    }
                }
            } else {
                if (bestTile != null) {
                    bestTile.placePiece(Color.BLACK);
                    if (checkWin(bestTile.x, bestTile.y, Color.BLACK)) {
                        gameEnded = true;
                        showGameOverDialog("很遗憾电脑赢了");
                    } else {
                        blackTurn = playerBlack;
                    }
                }
            }
        }

    }

    public void jsMove() {
        board[2][2].placePiece(Color.BLACK);  // 该位置放置黑棋
        board[2][4].placePiece(Color.BLACK);
        board[1][3].placePiece(Color.BLACK);
        board[3][3].placePiece(Color.BLACK);
        board[8][3].placePiece(Color.BLACK);  // 该位置放置黑棋
        board[8][2].placePiece(Color.BLACK);
        board[10][2].placePiece(Color.BLACK);
        board[10][1].placePiece(Color.BLACK);
        if(detectForbiddenPositions()){
            Circle forbiddenPiece = new Circle(TILE_SIZE * 0.3);
            // 设置禁手棋子的位置
            forbiddenPiece.setCenterX(TILE_SIZE * 0.5);
            forbiddenPiece.setCenterY(TILE_SIZE * 0.5);
            forbiddenPiece.setFill(Color.WHITE); // 设置填充颜色
            forbiddenPiece.setStroke(Color.RED); // 设置边框颜色
            forbiddenPiece.setStrokeWidth(2); // 设置边框宽度
            // 将禁手棋子添加到当前容器的子元素中
            boardPane.getChildren().add(forbiddenPiece);
        }

    }

    // 计算得分
    private int getScore(int x, int y, Color color) {
        int score = 0;
        // 检查横向、纵向、两个斜向
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] dir : directions) {
            int count = 1; // 连续棋子的数量
            // 向一个方向检查
            for (int i = 1; i < 5; i++) {
                int dx = x + i * dir[0];
                int dy = y + i * dir[1];
                if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece != null && board[dx][dy].piece.getFill() == color) {
                    count++;
                } else {
                    break;
                }
            }
            // 向相反方向检查
            for (int i = 1; i < 5; i++) {
                int dx = x - i * dir[0];
                int dy = y - i * dir[1];
                if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece != null && board[dx][dy].piece.getFill() == color) {
                    count++;
                } else {
                    break;
                }
            }
            // 根据连续棋子的数量给分(所有方向上连续相同颜色棋子的总得分)
            score += count * count;
        }
        return score;
    }

    // 显示游戏结束对话框
    private void showGameOverDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏结束");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 检查是否获胜
    /**
     * 检查是否获胜
     * @param x 棋子所在的x坐标
     * @param y 棋子所在的y坐标
     * @param color 棋子颜色
     * @return 是否获胜
     */
    private boolean checkWin(int x, int y, Color color) {
        // 检查所有方向
        for (int[] dir : new int[][]{{1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, 0}, {0, -1}, {-1, -1}, {-1, 1}}) {
            int count = 1;
            // 向正方向检查
            for (int i = 1; i < 5; i++) {
                int dx = x + i * dir[0];
                int dy = y + i * dir[1];
                if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece != null && board[dx][dy].piece.getFill() == color) {
                    count++;
                } else {
                    break;
                }
            }
            // 向反方向检查
            for (int i = 1; i < 5; i++) {
                int dx = x - i * dir[0];
                int dy = y - i * dir[1];
                if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece != null && board[dx][dy].piece.getFill() == color) {
                    count++;
                } else {
                    break;
                }
            }
            // 如果连续棋子数量达到5个或以上，返回获胜
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }
    //
    private boolean detectForbiddenPositions() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                // 只检查空位置
                if (board[x][y] == null && isForbiddenMove(x, y)) {
                    hasForbiddenPositions = true; // 标记存在禁止位置
                }
            }
        }
        return hasForbiddenPositions;
    }

    // 判断是否为禁手
    private boolean isForbiddenMove(int x, int y) {
        if (!blackTurn) return false; // 禁手规则仅适用于黑棋
        int threeCount = 0;
        // 检查四个方向
        for (int[] dir : new int[][]{{1, 0}, {0, 1}, {1, 1}, {1, -1}}) {
            int count = 1; // 包括当前落子点
            int emptyEnds = 0; // 空头的数量
            int continuous = 0; // 连续棋子的数量

            // 向一个方向检查
            for (int i = 1; i < 5; i++) {
                int dx = x + i * dir[0];
                int dy = y + i * dir[1];
                if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece != null && board[dx][dy].piece.getFill() == Color.BLACK) {
                    count++;
                    continuous++;
                } else if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece == null) {
                    emptyEnds++;
                    if (continuous == 1 && i < 4 && dx + dir[0] >= 0 && dx + dir[0] < WIDTH && dy + dir[1] >= 0 && dy + dir[1] < HEIGHT && board[dx + dir[0]][dy + dir[1]].piece != null && board[dx + dir[0]][dy + dir[1]].piece.getFill() == Color.BLACK) {
                        count++;
                    }
                    break;
                } else {
                    break;
                }
            }

            // 向相反方向检查
            continuous = 0; // 重置连续棋子的数量
            for (int i = 1; i < 5; i++) {
                int dx = x - i * dir[0];
                int dy = y - i * dir[1];
                if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece != null && board[dx][dy].piece.getFill() == Color.BLACK) {
                    count++;
                    continuous++;
                } else if (dx >= 0 && dx < WIDTH && dy >= 0 && dy < HEIGHT && board[dx][dy].piece == null) {
                    emptyEnds++;
                    if (continuous == 1 && i < 4 && dx - dir[0] >= 0 && dx - dir[0] < WIDTH && dy - dir[1] >= 0 && dy - dir[1] < HEIGHT && board[dx - dir[0]][dy - dir[1]].piece != null && board[dx - dir[0]][dy - dir[1]].piece.getFill() == Color.BLACK) {
                        count++;
                    }
                    break;
                } else {
                    break;
                }
            }

            // 判断是否为活三
            if (count == 3 && emptyEnds >= 2) {
                threeCount++;
            }
        }
        // 如果存在两个或更多的活三，则为禁手
        return (threeCount >= 2 );
    }

    // 棋子移动记录类
    private class Move {
        int x, y;
        Color color;

        Move(int x, int y, boolean isBlack) {
            this.x = x;
            this.y = y;
            this.color = isBlack ? Color.BLACK : Color.WHITE;
        }
    }

    // 悔棋功能
    public void undoMove() {
        // 检查玩家执黑棋的情况
        if (playerBlack) {
            // 如果历史记录不为空且游戏未结束
            if (!movesHistory.isEmpty() && !gameEnded) {
                // 弹出最后一步棋子移动记录
                Move lastMove = movesHistory.pop();
                // 获取最后一步棋子所在的格子
                Tile lastTile = board[lastMove.x][lastMove.y];
                // 移除最后一步棋子
                lastTile.getChildren().remove(lastTile.piece);
                lastTile.piece = null;
                // 恢复到上一步的回合状态
                blackTurn = lastMove.color == Color.BLACK;
                // 如果当前回合不是玩家回合，继续执行悔棋操作，直到回到玩家回合
                if (!blackTurn) {
                    undoMove();
                }
            }
        } else {
            // 检查玩家执白棋的情况
            if (movesHistory.size() > 1 && !gameEnded) {
                // 弹出最后一步棋子移动记录
                Move lastMove = movesHistory.pop();
                // 获取最后一步棋子所在的格子
                Tile lastTile = board[lastMove.x][lastMove.y];
                // 移除最后一步棋子
                lastTile.getChildren().remove(lastTile.piece);
                lastTile.piece = null;
                // 恢复到上一步的回合状态
                blackTurn = lastMove.color == Color.BLACK;
                // 如果当前回合是玩家回合，继续执行悔棋操作，直到回到电脑回合
                if (blackTurn) {
                    undoMove();
                }
            }
        }
    }



    public static void main(String[] args) {

        launch(args);
    }


}

