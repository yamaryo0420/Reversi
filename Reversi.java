import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

class Stone {
    public final static int black = 1;
    public final static int white = 2;
    private int obverse;
    // コンストラクタの初期化
    Stone(){
        obverse = 0; //初期値は0とし、非配置とする
    }
    
    // 表面の色を設定
    void setObverse(int color) {
        if(color == black || color == white) {
            obverse = color;
        }
        else {
            System.out.println("黒か白でなければいけません");
        }
    }

    //表面の色で中心p、半径radの円を塗りつぶす
    void paint(Graphics g, Point p, int rad) {
        if (obverse == black) {
            g.setColor(Color.black); //ペンを黒に設定
            g.fillOval(p.x-rad, p.y-rad, rad*2, rad*2); //円を描画
        }
        else if (obverse == white) {
            g.setColor(Color.white); //ペンを白に設定
            g.fillOval(p.x-rad, p.y-rad, rad*2, rad*2); //円を描画
        }
    }

    //表面の色を取得
    int getObverse(){
        return obverse;
    }

    //白黒を反転
    void doReverse(){
        if(obverse == black) obverse = white;
        else if(obverse == white) obverse = black;
    }
}

class Board {
    public Stone[][] board = new Stone[8][8];
    public int num_grid_black;
    public int num_grid_white;
    private Point[] direction = new Point[8];
    public int[][] eval_black = new int[8][8];
    public int[][] eval_white = new int[8][8];

    
    // コンストラクタの初期化
    Board() {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                board[i][j] = new Stone();
            }
        }
        board[3][3].setObverse(1);
        board[4][4].setObverse(1);
        board[3][4].setObverse(2);
        board[4][3].setObverse(2);

        //方向ベクトルの作成
        direction[0] = new Point(1, 0);//右方向
        direction[1] = new Point(1, 1);//右下方向
        direction[2] = new Point(0, 1);//下方向
        direction[3] = new Point(-1, 1);//左下方向
        direction[4] = new Point(-1, 0);//左方向
        direction[5] = new Point(-1, -1);//左上方向
        direction[6] = new Point(0, -1);//上方向
        direction[7] = new Point(1, -1);//右上方向
    }

    //画面描画
    void paint(Graphics g, int unit_size){
        //背景
        g.setColor(Color.black);
        g.fillRect(0, 0, unit_size*10, unit_size*10);
        //盤面
        g.setColor(new Color(0, 85, 0));
        g.fillRect(unit_size, unit_size, unit_size*8, unit_size*8);
        //横線
        g.setColor(Color.black);
        for(int i = 0; i < 9; i++){
            g.drawLine(unit_size, unit_size*(i+1), unit_size*9, unit_size*(i+1));
        }
        //縦線
        g.setColor(Color.black);
        for(int i = 0; i < 9; i++){
            g.drawLine(unit_size*(i+1), unit_size, unit_size*(i+1), unit_size*9);
        }
        //目印
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){
                g.fillRect((4*(j+1)-1)*unit_size-unit_size/20, (4*(i+1)-1)*unit_size-unit_size/20, unit_size/10, unit_size/10);
            }
        }
        // 石
        Point p = new Point();
        int rad = (int)(unit_size * 0.8 / 2);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                p.x = unit_size * (i + 1) + unit_size / 2;
                p.y = unit_size * (j + 1) + unit_size / 2;
                board[i][j].paint(g, p, rad);
            }
        }
    }

    //盤面の枠内かチェック
    boolean isOnBoard(int x, int y){
        if(0 <= x && x <= 7 && 0 <= y && y <= 7) return true;
        else return false;
    }
    
    //クリックされたマス目に石を配置
    void setStone(int x, int y, int s){
        board[x][y].setObverse(s);
    }

    //盤面を評価
    void evaluateBoard(){
        num_grid_black = 0; //黒石が置けるマス目の数
        num_grid_white = 0; //白石が置けるマス目の数
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                eval_black[i][j] = countReverseStone(i, j, 1);
                if(eval_black[i][j] > 0) num_grid_black++;
                eval_white[i][j] = countReverseStone(i, j, 2);
                if(eval_white[i][j] > 0) num_grid_white++;
            }
        }
    }

    //盤面上の石sの数をカウント
    int countStone(int s){
        int cnt = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(board[i][j].getObverse() == s) cnt++;
            }
        }
        return cnt;
    }

    // 盤面(x, y)から方向dに向かって石を順に取得
    ArrayList<Integer> getLine(int x, int y, Point d){
        ArrayList<Integer> line = new ArrayList<Integer>();
        int cx = x + d.x;
        int cy = y + d.y;
        while(isOnBoard(cx, cy) && board[cx][cy].getObverse() != 0){
            line.add(board[cx][cy].getObverse());
            cx += d.x;
            cy += d.y;
        }
        return line;
    }

    // 盤面(x, y)に石sを置いた場合に反転できる石の数をカウント
    int countReverseStone(int x, int y, int s){
        //すでに石が置かれていたら、置けない
        if(board[x][y].getObverse() != 0) return -1;
        //８方向をチェック
        int cnt = 0;
        for(int d = 0; d < 8; d++){
            ArrayList<Integer> line = new ArrayList<Integer>();
            line = getLine(x, y, direction[d]);
            int n = 0;
            while (n < line.size() && line.get(n) != s) n++;
            if(0 < n && n < line.size()) cnt += n;
        }
        return cnt;
    }

    // 盤面(x, y)に石sを置き、他の石を反転
    void setStoneAndReverse(int x, int y, int s){
        setStone(x, y, s);
        //８方向をチェック
        for(int d = 0; d < 8; d++){
            ArrayList<Integer> line = new ArrayList<Integer>();
            line = getLine(x, y, direction[d]);
            int n = 0;
            Boolean flag = false;
            for(int i = 0; i < line.size(); i++){
                if(line.get(i) == s){
                    n = i;
                    break;
                }
                flag = true;
            }
            if(flag && n != 0){
                int cx = x + direction[d].x;
                int cy = y + direction[d].y;
                for(int i = 0; i < n; i++) {
                    if(line.get(i) != s){
                        board[cx][cy].doReverse();
                        cx += direction[d].x;
                        cy += direction[d].y;
                    }
                }
            }
        }
    }

    // 盤面の評価をコンソールに表示（テスト用）
    void printEval(){
        System.out.println("Black(1):");
        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                System.out.printf("%2d ", eval_black[x][y]);
            }
            System.out.println("");
        }
        System.out.println("White(2):");
        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                System.out.printf("%2d ", eval_white[x][y]);
            }
            System.out.println("");
        }
    }
}

class Player {
    public final static int type_human = 0;
    public final static int type_computer = 1;
    private int color; // Stone.black or Stone.white
    private int type; //type_human or type_computer

    Player (int c, int t){
        if(c == Stone.black || c == Stone.white) color = c;
        else{
            System.out.println("プレイヤーの石は黒か白でなければいけません:" + c);
            System.exit(0);
        }
        if(t == type_human || t == type_computer) type = t;
        else{
            System.out.println("プレイヤーは人間かコンピュータでなければいけません:" + t);
            System.exit(0);
        }
    }

    int getColor(){
        return color;
    }

    int getType(){
        return type;
    }

    // 配置できるマス目の中からランダムに選ぶ（ランダム）
    Point tactics1(Board bd){
        ArrayList<Point> put_black = new ArrayList<Point>();
        ArrayList<Point> put_white = new ArrayList<Point>();
        Random rand = new Random();        
        if(color == Stone.black){
            for (int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    if(bd.eval_black[i][j] > 0) put_black.add(new Point(i, j));
                }
            }
            if(put_black.size() > 0){
                int num = rand.nextInt(put_black.size());
                return put_black.get(num);
            }
        }
        else if(color == Stone.white){
            for (int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    if(bd.eval_white[i][j] > 0) put_white.add(new Point(i, j));
                }
            }
            if(put_white.size() > 0){
                int num = rand.nextInt(put_white.size());
                return put_white.get(num);
            }
        }
        return (new Point(-1, -1)); //配置可能な場所がない場合
    }

    // ひっくり返せる相手の石が最も多いマス目を選び、複数ある場合はランダムに選ぶ（貪欲）
    Point tactics2(Board bd){
        ArrayList<Point> put_black = new ArrayList<Point>();
        ArrayList<Point> put_white = new ArrayList<Point>();
        Random rand = new Random();
        int max_num_stone = 0;
        if(color == Stone.black){
            for (int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    if(bd.eval_black[i][j] > max_num_stone) max_num_stone = bd.eval_black[i][j];
                }
            }
            for(int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    if(max_num_stone == bd.eval_black[i][j]) put_black.add(new Point(i, j));
                }
            }
            if(put_black.size() > 0){
                int num = rand.nextInt(put_black.size());
                return put_black.get(num);
            }
        }
        else if(color == Stone.white){
            for (int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    if(bd.eval_white[i][j] > max_num_stone) max_num_stone = bd.eval_white[i][j];
                }
            }
            for(int i = 0; i < 8; i++){
                for(int j = 0; j < 8; j++){
                    if(max_num_stone == bd.eval_white[i][j]) put_white.add(new Point(i, j));
                }
            }
            if(put_white.size() > 0){
                int num = rand.nextInt(put_white.size());
                return put_white.get(num);
            }
        }
        return (new Point(-1, -1)); //配置可能な場所がない場合
    }

    // ひっくり返せる相手の石の数と盤面の特性（角、端が有利）を考慮してマス目を選ぶ（盤面考慮）
    Point tactics3(Board bd){
        int max_num_stone = 0;
        int max_i = 0;
        int max_j = 0;
        int[][] weight = new int[8][8];
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                //角
                if((i == 0 || i == 7) && (j == 0 || j == 7)) weight[i][j] = 4;
                // 角以外の端の4列
                else if(i == 0 && j != 0 && j != 7) weight[i][j] = 3;
                else if(i != 0 && i != 7 && j == 0) weight[i][j] = 3;
                else if(i != 0 && i != 7 && j == 7) weight[i][j] = 3;
                else if(i == 7 && j != 0 && j != 7) weight[i][j] = 3;
                // 端の一つ内側の4列
                else if((i != 0 && i != 7) && (j != 0 && j != 7)) weight[i][j] = 2;
                // その他
                else weight[i][j] = 1;
            }
        }
        if(color == Stone.black) {
            for (int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    if(bd.eval_black[i][j] * weight[i][j] > max_num_stone) {
                        max_num_stone = bd.eval_black[i][j] * weight[i][j];
                        max_i = i;
                        max_j = j;
                    }
                }
            }
            if(max_num_stone > 0) return (new Point(max_i, max_j));
        }
        else if(color == Stone.white) {
            for (int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    if(bd.eval_white[i][j] * weight[i][j]> max_num_stone) {
                        max_num_stone = bd.eval_white[i][j] * weight[i][j];
                        max_i = i;
                        max_j = j;
                    }
                }
            }
            if(max_num_stone > 0) return (new Point(max_i, max_j));
        }
        return (new Point(-1, -1)); //配置可能な場所がない場合
    }

    // ひっくり返せる相手の石の数と盤面の特性（角と端が有利、角から斜め１つ内側と角の隣は不利）を考慮してマス目を選ぶ（盤面考慮②）
    Point tactics4(Board bd){
        double max_num_stone = 0;
        int max_i = 0;
        int max_j = 0;
        double[][] weight = {
            {1000, 1, 10, 100, 100, 10, 1, 1000},
            {1, 0.1, 20, 20, 20, 20, 0.1, 1},
            {10, 20, 5, 5, 5, 5, 20, 10},
            {100, 20, 5, 5, 5, 5, 20, 100},
            {100, 20, 5, 5, 5, 5, 20, 100},
            {10, 20, 5, 5, 5, 5, 20, 10},
            {1, 0.1, 20, 20, 20, 20, 0.1, 1},
            {1000, 1, 10, 100, 100, 10, 1, 1000}
        };
        if(color == Stone.black) {
            if(bd.board[0][0].getObverse() == Stone.black) {
                weight[0][1] = 1000;
                weight[1][0] = 1000;
                weight[1][1] = 1000;
            }
            if(bd.board[0][7].getObverse() == Stone.black) {
                weight[0][6] = 1000;
                weight[1][6] = 1000;
                weight[1][7] = 1000;
            }if(bd.board[7][0].getObverse() == Stone.black) {
                weight[6][0] = 1000;
                weight[6][1] = 1000;
                weight[7][1] = 1000;
            }if(bd.board[7][7].getObverse() == Stone.black) {
                weight[6][6] = 1000;
                weight[6][7] = 1000;
                weight[7][6] = 1000;
            }
            for (int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    if(bd.eval_black[i][j] > 0){
                        if(bd.eval_black[i][j] * weight[i][j] >= max_num_stone) {
                            max_num_stone = bd.eval_black[i][j] * weight[i][j];
                            max_i = i;
                            max_j = j;
                        }
                    } 
                }
            }
            if(max_num_stone > 0) return (new Point(max_i, max_j));
        }
        else if(color == Stone.white) {
            if(bd.board[0][0].getObverse() == Stone.white) {
                weight[0][1] = 1000;
                weight[1][0] = 1000;
                weight[1][1] = 1000;
            }
            if(bd.board[0][7].getObverse() == Stone.white) {
                weight[0][6] = 1000;
                weight[1][6] = 1000;
                weight[1][7] = 1000;
            }if(bd.board[7][0].getObverse() == Stone.white) {
                weight[6][0] = 1000;
                weight[6][1] = 1000;
                weight[7][1] = 1000;
            }if(bd.board[7][7].getObverse() == Stone.white) {
                weight[6][6] = 1000;
                weight[6][7] = 1000;
                weight[7][6] = 1000;
            }
            for (int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    if(bd.eval_white[i][j] > 0){
                        if(bd.eval_white[i][j] * weight[i][j] >= max_num_stone) {
                            max_num_stone = bd.eval_white[i][j] * weight[i][j];
                            max_i = i;
                            max_j = j;
                        }
                    }
                }
            }
            if(max_num_stone > 0) return (new Point(max_i, max_j));
        }
        return (new Point(-1, -1)); //配置可能な場所がない場合
    }

    //次に石を置くマス目を決定
    Point nextMove(Board bd, Point p, int t){
        if(type == type_human) return p;
        else if(type == type_computer){
            if(t == 1) return tactics1(bd);
            else if(t == 2) return tactics2(bd);
            else if(t == 3) return tactics3(bd);
            else if(t == 4) return tactics4(bd);
        }
        return (new Point(-1, -1)); //通常はありえない
    }
}

public class Reversi extends JPanel{
    public final static int UNIT_SIZE = 80;
    Board board = new Board();
    private int turn; //1:Stone.black 2:Stone.white
    private Player[] player = new Player[2];
    public static int tactics;

    //コンストラクタの初期化
    public Reversi(){
        setPreferredSize(new Dimension(UNIT_SIZE*10, UNIT_SIZE*10)); //パネルサイズを800x800に設定
        addMouseListener(new MouseProc());
        player[0] = new Player(Stone.black, Player.type_human);
        player[1] = new Player(Stone.white, Player.type_computer);
    }

    //画面描画
    public void paintComponent(Graphics g){
        board.paint(g, UNIT_SIZE);
        //画面メッセージ
        String msg1 = "";
        g.setColor(Color.white);
        if(turn == Stone.white) msg1 = "白の番です";
        else{
            turn = Stone.black; //初ターン
            msg1 = "黒の番です";
        }
        if(player[turn-1].getType() == Player.type_computer) msg1 += "(考えています)";

        String msg2 = "[黒:" + board.countStone(Stone.black) + ", 白:" + board.countStone(Stone.white) + "]";
        g.drawString(msg1, UNIT_SIZE/2, UNIT_SIZE/2);
        g.drawString(msg2, UNIT_SIZE/2, 19*UNIT_SIZE/2);
    }

    //起動
    public static void main(String[] args) {
        tactics = Integer.parseInt(args[0]);// コンピュータのレベルを取得
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(new Reversi());
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // 終了メッセージダイアログの表示
    void EndMessageDialog() {
        int black = board.countStone(Stone.black);
        int white = board.countStone(Stone.white);
        String str = "[黒:" + black + ",白:" + white + "]で";

        if(black > white) str += "黒の勝ち";
        else if(black < white) str += "白の勝ち";
        else str += "引き分け";

        JOptionPane.showMessageDialog(this, str, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    // メッセージダイアログを表示
    void MessageDialog(String str){
        JOptionPane.showMessageDialog(this, str, "情報", JOptionPane.INFORMATION_MESSAGE);
    }

    //手番を管理
    void changeTurn(){
        if(turn == Stone.black) turn = Stone.white;
        else if(turn == Stone.white) turn = Stone.black;
    }

    //クリックされたときの処理用のクラス
    class MouseProc extends MouseAdapter {
        public void mouseClicked(MouseEvent me) {
            Point point = me.getPoint();
            int btn = me.getButton();
            Point gp = new Point();
            gp.x = point.x / UNIT_SIZE - 1;
            gp.y = point.y / UNIT_SIZE - 1;
            
            if(!board.isOnBoard(gp.x, gp.y)) return; //盤面の外
            removeMouseListener(this); //一時的にマウスイベントの処理を止める
            board.evaluateBoard(); //ターン前の評価
            //プレイヤー（人間）の手番
            if(player[turn-1].getType() == Player.type_human) {
                if((player[turn-1].getColor() == Stone.black && board.num_grid_black == 0) || (player[turn-1].getColor() == Stone.white && board.num_grid_white == 0)){
                    MessageDialog("あなたはパスです");
                    changeTurn();
                    repaint();
                }
                else if((player[turn-1].getColor() == Stone.black && board.eval_black[gp.x][gp.y] > 0 && btn == MouseEvent.BUTTON1) || (player[turn-1].getColor() == Stone.white && board.eval_white[gp.x][gp.y] > 0 && btn == MouseEvent.BUTTON3)) {
                    Point nm = player[turn-1].nextMove(board, gp, tactics);
                    board.setStoneAndReverse(nm.x, nm.y, player[turn-1].getColor());
                    changeTurn();
                    board.evaluateBoard(); //ターン後の評価
                    // board.printEval();
                    repaint();
                    //ゲーム終了確認
                    if(board.num_grid_black == 0 && board.num_grid_white == 0) EndMessageDialog();
                }
                //人間対人間の時
                if(player[turn-1].getType() == Player.type_human) addMouseListener(this);
            }
            //コンピュータ（自動）の手番
            if(player[turn-1].getType() == Player.type_computer){
                Thread th = new TacticsThread();
                th.start();
            }
        }
    }
    //コンピュータとの対戦用スレッド
    class TacticsThread extends Thread {
        public void run(){
            try{
                Thread.sleep(2000); //2秒間待つ
                Point nm = player[turn-1].nextMove(board, new Point(-1, -1), tactics);
                if(nm.x == -1 && nm.y == -1) MessageDialog("相手はパスです");
                else board.setStoneAndReverse(nm.x, nm.y, player[turn-1].getColor());
                changeTurn();
                board.evaluateBoard(); //ターン後の評価
                // board.printEval();
                repaint();
                addMouseListener(new MouseProc()); //マウスイベント処理を再開する
                //ゲーム終了確認
                if(board.num_grid_black == 0 && board.num_grid_white == 0) EndMessageDialog();
            } catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }
}
