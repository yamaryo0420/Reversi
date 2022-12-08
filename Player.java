import java.awt.*;
import java.util.*;

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