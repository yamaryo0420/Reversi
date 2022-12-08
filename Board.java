import java.awt.*;
import java.util.*;

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